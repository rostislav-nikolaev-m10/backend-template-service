package com.m10.testkit.redisui;

import java.time.Duration;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.m10.testkit.redis.RedisContainerManager;


public class RedisUIContainerManager {

    public static final String BEAN_NAME = RedisUIContainerManager.class.getName();
    public static final int REDIS_UI_PORT = 5540;

    private final RedisUIContainerDefinition containerDefinition;
    private final RedisContainerManager redisContainerManager;

    private GenericContainer<?> container;

    public RedisUIContainerManager(
        RedisUIContainerDefinition containerDefinition,
        RedisContainerManager redisContainerManager
    ) {
        this.containerDefinition = containerDefinition;
        this.redisContainerManager = redisContainerManager;
    }

    synchronized public GenericContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer();
        container.start();
        patchSettings();
        return container;
    }

    private GenericContainer<?> createContainer() {
        var redisIp = redisContainerManager.getContainer().getCurrentContainerInfo().getNetworkSettings().getNetworks()
            .entrySet()
            .stream().findFirst()
            .orElseThrow()
            .getValue()
            .getIpAddress();
        var redisUI = new GenericContainer<>(DockerImageName.parse(containerDefinition.containerName()))
            .withExposedPorts(REDIS_UI_PORT)
            .waitingFor(
                Wait
                    .forHttp("/api/health")
                    .forPort(REDIS_UI_PORT)
                    .withStartupTimeout(Duration.ofSeconds(30))
            )
            .withLabel("com.testcontainers.desktop.service", "component-tests-redis-ui")
//            .withEnv("RI_REDIS_HOST", redisIp)
            .withEnv("RI_REDIS_HOST", "dev-cache-cluster-replication-group.9ykv65.ng.0001.euc1.cache.amazonaws.com")
            .withEnv("RI_REDIS_PORT", String.valueOf(6379))
            .withReuse(true);
        return redisUI;
    }

    private void patchSettings() {
        WebTestClient.bindToServer()
            .baseUrl("http://" + getContainer().getHost() + ":" + getContainer().getMappedPort(REDIS_UI_PORT))
            .build()
            .patch()
            .uri("/api/settings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "agreements", Map.of(
                    "eula", true,
                    "analytics", false,
                    "notifications", false,
                    "encryption", false
                )
            ))
            .exchange()
            .expectStatus().isOk();
    }

}
