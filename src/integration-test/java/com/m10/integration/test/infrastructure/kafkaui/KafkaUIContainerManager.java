package com.m10.integration.test.infrastructure.kafkaui;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;

@Slf4j
@RequiredArgsConstructor
public class KafkaUIContainerManager {

    public static final String BEAN_NAME = KafkaUIContainerManager.class.getName();

    private final KafkaUIContainerDefinition containerDefinition;

    private GenericContainer<?> container;

    synchronized public GenericContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer();
        container.start();
        return container;
    }

    private GenericContainer<?> createContainer() {
//        try (var dockerClient = DockerClientFactory.instance().client()) {
//            var kafkaContainerInspect =
//                    dockerClient.inspectContainerCmd(DefaultKafkaTestContainerExtension.getKafkaContainer().getContainerId())
//                        .exec();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return new GenericContainer<>(DockerImageName.parse(containerDefinition.containerName()))
            .withExposedPorts(8080)
            .waitingFor(
                Wait
                    .forHttp("/api/clusters")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(30))
            )
            .withNetwork(Docker.network)
            .withLabel("com.testcontainers.desktop.service", "component-tests-kafka-ui")
            .withEnv("KAFKA_CLUSTERS_0_NAME", "dev-cluster")
//                .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", kafkaContainerInspect.getNetworkSettings().getIpAddress() + ":29093")
            .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", "b-1.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092,b-2.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092,b-3.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092")
            .withEnv("KAFKA_CLUSTERS_0_SCHEMAREGISTRY", "http://host.docker.internal:16161")
            .withReuse(true);
    }

}
