package com.m10.demo.integration_tests.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.vault.client.VaultHttpHeaders;
import org.testcontainers.vault.VaultContainer;

import java.util.List;

//import static com.m10.demo.integration_tests.extension.DefaultVaultTestContainerExtension.VAULT_TOKEN;

public class VaultTestUtils {
    private VaultTestUtils() {
    }

    //    public static void flushVault(VaultContainer<?> vaultContainer) {
//        String baseUrl = "http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort();
//        WebTestClient client =
//                WebTestClient.bindToServer()
//                        .baseUrl(baseUrl)
//                        .build();
//        EntityExchangeResult<VaultResponse> response = client.get()
//                .uri("/v1/transit/keys?list=true")
//                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                .exchange()
//                .expectBody(VaultResponse.class)
//                .returnResult();
//        if (response.getStatus() != HttpStatus.OK) return;
//        List<String> keys = response.getResponseBody().data().keys();
//        keys.stream().forEach(key -> {
//            client.delete()
//                    .uri("/v1/transit/keys/" + key)
//                    .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                    .exchange()
//                    .expectStatus().is2xxSuccessful();
//        });
//    }
//
//    public static void addVaultPolicy(VaultContainer<?> vaultContainer) {
//        String baseUrl = "http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort();
//        WebTestClient client =
//                WebTestClient.bindToServer()
//                        .baseUrl(baseUrl)
//                        .build();
//        String policyName = "transit-global-policy";
//        String policy = """
//        path "transit/keys/*" {
//          capabilities = ["create", "update", "read", "delete"]
//        }
//
//        path "transit/keys/*/rotate" {
//          capabilities = ["update"]
//        }
//        """;
//        client.put()
//                .uri("/v1/sys/policy/{name}", policyName)
//                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue("""
//            {
//              "policy": %s,
//              "policy_type": "acl"
//            }
//            """.formatted(toJsonString(policy)))
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    public static void createKey(String keyName, VaultContainer<?> vaultContainer) {
//        String baseUrl = "http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort();
//        WebTestClient client =
//                WebTestClient.bindToServer()
//                        .baseUrl(baseUrl)
//                        .build();
//        client.post()
//                .uri("/v1/transit/keys/{keyName}", keyName)
//                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue("""
//                                        {
//                                            "type": "aes256-gcm96"
//                                        }
//                        """)
//                .exchange()
//                .expectStatus().isNoContent();
//        client.post()
//                .uri("/v1/transit/keys/{keyName}/config", keyName)
//                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue("""
//                                        {
//                                            "deletion_allowed": true
//                                        }
//                        """)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    public static void rotateKey(String keyName, VaultContainer<?> vaultContainer) {
//        String baseUrl = "http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort();
//        WebTestClient client =
//                WebTestClient.bindToServer()
//                        .baseUrl(baseUrl)
//                        .build();
//        client.post()
//                .uri("/v1/transit/keys/{keyName}/rotate", keyName)
//                .header(VaultHttpHeaders.VAULT_TOKEN, VAULT_TOKEN)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    private static String toJsonString(String hcl) {
//        return "\"" + hcl
//                .replace("\\", "\\\\")
//                .replace("\"", "\\\"")
//                .replace("\n", "\\n") + "\"";
//    }

}
