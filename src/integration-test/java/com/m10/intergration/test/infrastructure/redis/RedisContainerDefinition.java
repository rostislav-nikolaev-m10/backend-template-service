package com.m10.intergration.test.infrastructure.redis;

public record RedisContainerDefinition(
    String instanceName,
    String containerName
) {
}
