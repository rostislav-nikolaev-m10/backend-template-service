package com.m10.intergration.test.infrastructure.postgres;

public record PostgresContainerDefinition(
    String instanceName,
    String containerName
) {
}
