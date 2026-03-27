package com.m10.integration.test.infrastructure.kafkaui;

import java.util.Objects;

public record KafkaUIContainerDefinition(
    String containerName
) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KafkaUIContainerDefinition that = (KafkaUIContainerDefinition) o;
        return Objects.equals(containerName, that.containerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerName);
    }

}
