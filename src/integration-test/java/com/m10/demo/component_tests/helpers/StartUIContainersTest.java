package com.m10.demo.component_tests.helpers;

import com.m10.demo.component_tests.extension.DefaultKafkaTestContainerExtension;
import com.m10.demo.component_tests.extension.DefaultRedisTestContainerExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

@Disabled
public class StartUIContainersTest extends InitComponentTest {

    @Test
    public void startRedisAndKafkaUI() {
        var dockerClient = DockerClientFactory.instance().client();
        var redisContainerInspect =
                dockerClient.inspectContainerCmd(DefaultRedisTestContainerExtension.getRedisContainer().getContainerId())
                        .exec();
        var redisUI = new GenericContainer<>(DockerImageName.parse("redis/redisinsight:2.70"))
                .withExposedPorts(5540)
                .waitingFor(
                        Wait
                                .forHttp("/api/health")
                                .forPort(5540)
                                .withStartupTimeout(Duration.ofSeconds(30))
                )
                .withLabel("com.testcontainers.desktop.service", "component-tests-redis-ui")
                .withEnv("RI_REDIS_HOST", redisContainerInspect.getNetworkSettings().getIpAddress())
                .withEnv("RI_REDIS_PORT", String.valueOf(DefaultRedisTestContainerExtension.REDIS_PORT))
                .withReuse(true);
        redisUI.start();
        patchSettings(redisUI);

        var kafkaContainerInspect =
                dockerClient.inspectContainerCmd(DefaultKafkaTestContainerExtension.getKafkaContainer().getContainerId())
                        .exec();

        var kafkaUI = new GenericContainer<>(DockerImageName.parse("kafbat/kafka-ui:7073c72"))
                .withExposedPorts(8080)
                .waitingFor(
                        Wait
                                .forHttp("/api/clusters")
                                .forPort(8080)
                                .withStartupTimeout(Duration.ofSeconds(30))
                )
                .withLabel("com.testcontainers.desktop.service", "component-tests-kafka-ui")
                .withEnv("KAFKA_CLUSTERS_0_NAME", "bakikart-cluster")
                .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", kafkaContainerInspect.getNetworkSettings().getIpAddress() + ":29093")
                .withReuse(true);
        kafkaUI.start();
    }

    private void patchSettings(GenericContainer<?> redisInsight) {
        WebTestClient.bindToServer()
                .baseUrl("http://" + redisInsight.getHost() + ":" + redisInsight.getMappedPort(5540))
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
