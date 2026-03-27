package com.m10.integration.test.infrastructure.postgres;

public record PostgresContainerDefinition(
    String instanceName,
    String containerName
) {
}
