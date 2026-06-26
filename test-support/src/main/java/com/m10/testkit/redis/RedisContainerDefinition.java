package com.m10.testkit.redis;

public record RedisContainerDefinition(
    String instanceName,
    String containerName
) {
}
