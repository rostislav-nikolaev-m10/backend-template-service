package com.m10.testkit.vault;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.vault.client.VaultHttpHeaders;
import org.testcontainers.vault.VaultContainer;

import com.m10.testkit.core.Docker;


public class VaultContainerManager {

    public static final String VAULT_TOKEN = "test";
    public static final String BEAN_NAME = VaultContainerManager.class.getName();

    private final VaultContainerDefinition containerDefinition;

    private VaultContainer<?> container;

    public VaultContainerManager(VaultContainerDefinition containerDefinition) {
        this.containerDefinition = containerDefinition;
    }

    synchronized public VaultContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer(containerDefinition.containerName());
        container.start();
        return container;
    }

    private VaultContainer<?> createContainer(String imageName) {
        // NB: транзит-движок НЕ монтируем через withInitCommand — она неидемпотентна и
        // при withReuse(true) повторно выполняется на переиспользованном контейнере,
        // падая с "path is already in use". Монтируем идемпотентно через API: enableTransitEngine().
        return new VaultContainer<>(imageName)
            .withVaultToken("test")
            .withLabel("com.testcontainers.desktop.service", "component-tests-vault")
            .withNetwork(Docker.network)
            .withReuse(true);
    }

    /**
     * Идемпотентно монтирует transit-движок по пути transit/test-engine.
     * Безопасно при переиспользовании контейнера: если движок уже смонтирован — ничего не делает.
     */
    public void enableTransitEngine() {
        String baseUrl = "http://" + getContainer().getHost() + ":" + getContainer().getFirstMappedPort();
        WebTestClient client =
            WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
        boolean alreadyMounted = client.get()
            .uri("/v1/sys/mounts/transit/test-engine/tune")
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .exchange()
            .returnResult(Void.class)
            .getStatus()
            .is2xxSuccessful();
        if (alreadyMounted) {
            return;
        }
        client.post()
            .uri("/v1/sys/mounts/transit/test-engine")
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "type": "transit"
                }
                """)
            .exchange()
            .expectStatus().is2xxSuccessful();
    }

    public void flushVault() {
        String baseUrl = "http://" + getContainer().getHost() + ":" + getContainer().getFirstMappedPort();
        WebTestClient client =
            WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
        EntityExchangeResult<VaultResponse> response = client.get()
            .uri("/v1/transit/test-engine/keys?list=true")
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .exchange()
            .expectBody(VaultResponse.class)
            .returnResult();
        if (response.getStatus() != HttpStatus.OK) return;
        List<String> keys = response.getResponseBody().data().keys();
        keys.stream().forEach(key -> {
            client.delete()
                .uri("/v1/transit/test-engine/keys/" + key)
                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful();
        });
    }

    public void addVaultPolicy() {
        String baseUrl = "http://" + getContainer().getHost() + ":" + getContainer().getFirstMappedPort();
        WebTestClient client =
            WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
        String policyName = "transit-global-policy";
        String policy = """
            path "transit/keys/*" {
              capabilities = ["create", "update", "read", "delete"]
            }
            
            path "transit/keys/*/rotate" {
              capabilities = ["update"]
            }
            """;
        client.put()
            .uri("/v1/sys/policy/{name}", policyName)
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "policy": %s,
                  "policy_type": "acl"
                }
                """.formatted(toJsonString(policy)))
            .exchange()
            .expectStatus().isNoContent();
    }

    public void createKey(String keyName) {
        String baseUrl = "http://" + getContainer().getHost() + ":" + getContainer().getFirstMappedPort();
        WebTestClient client =
            WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
        client.post()
            .uri("/v1/transit/test-engine/keys/{keyName}", keyName)
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                                {
                                    "type": "aes256-gcm96"
                                }
                """)
            .exchange()
            .expectStatus().isNoContent();
        client.post()
            .uri("/v1/transit/test-engine/keys/{keyName}/config", keyName)
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                                {
                                    "deletion_allowed": true
                                }
                """)
            .exchange()
            .expectStatus().isNoContent();
    }

    public void rotateKey(String keyName) {
        String baseUrl = "http://" + getContainer().getHost() + ":" + getContainer().getFirstMappedPort();
        WebTestClient client =
            WebTestClient.bindToServer()
                .baseUrl(baseUrl)
                .build();
        client.post()
            .uri("/v1/transit/test-engine/keys/{keyName}/rotate", keyName)
            .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
            .exchange()
            .expectStatus().isNoContent();
    }

    private String toJsonString(String hcl) {
        return "\"" + hcl
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n") + "\"";
    }

}
