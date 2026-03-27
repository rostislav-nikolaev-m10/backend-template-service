package com.m10.integration.test.infrastructure.redis;

public record RedisContainerDefinition(
    String instanceName,
    String containerName
) {
}
