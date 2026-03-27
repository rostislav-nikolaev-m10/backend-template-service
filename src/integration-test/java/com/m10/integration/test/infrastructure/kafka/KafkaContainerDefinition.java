package com.m10.integration.test.infrastructure.kafka;

import java.util.Arrays;
import java.util.Objects;

public record KafkaContainerDefinition(
    String containerName,
    String[] initialTopics
) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KafkaContainerDefinition that = (KafkaContainerDefinition) o;
        return Objects.equals(containerName, that.containerName) && Objects.deepEquals(initialTopics, that.initialTopics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerName, Arrays.hashCode(initialTopics));
    }

}
