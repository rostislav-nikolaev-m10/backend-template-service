package com.m10.integration.test.infrastructure.kafkaui;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;
import com.m10.integration.test.infrastructure.kafka.KafkaContainerManager;


public class KafkaUIContainerManager {

    public static final String BEAN_NAME = KafkaUIContainerManager.class.getName();

    private final KafkaUIContainerDefinition containerDefinition;
    private final KafkaContainerManager kafkaContainerManager;

    private GenericContainer<?> container;

    public KafkaUIContainerManager(
        KafkaUIContainerDefinition containerDefinition,
        KafkaContainerManager kafkaContainerManager
    ) {
        this.containerDefinition = containerDefinition;
        this.kafkaContainerManager = kafkaContainerManager;
    }

    synchronized public GenericContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        container = createContainer();
        container.start();
        return container;
    }

    private GenericContainer<?> createContainer() {
        var kafkaIp = kafkaContainerManager.getContainer().getCurrentContainerInfo().getNetworkSettings().getNetworks()
            .entrySet()
            .stream().findFirst()
            .orElseThrow()
            .getValue()
            .getIpAddress();
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
            .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", kafkaIp + ":9093")
//            .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", "b-1.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092,b-2.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092,b-3.dev-msk-cluster.y7r5c4.c6.kafka.eu-central-1.amazonaws.com:9092")
            .withEnv("KAFKA_CLUSTERS_0_SCHEMAREGISTRY", "http://host.docker.internal:16161")
            .withReuse(true);
    }

}
