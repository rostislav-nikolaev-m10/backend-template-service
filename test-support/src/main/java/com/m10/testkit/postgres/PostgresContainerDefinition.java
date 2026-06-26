package com.m10.testkit.postgres;

public record PostgresContainerDefinition(
    String instanceName,
    String containerName
) {
}
