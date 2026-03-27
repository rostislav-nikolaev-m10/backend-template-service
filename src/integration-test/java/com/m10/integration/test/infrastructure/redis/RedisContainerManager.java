package com.m10.integration.test.infrastructure.redis;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;


public class RedisContainerManager {

    public static final String BEAN_NAME = RedisContainerManager.class.getName();
    public static final int REDIS_PORT = 6379;

    private final RedisContainerDefinition containerDefinition;

    private RedisContainer container;
    private RedisClient redisClient;

    public RedisContainerManager(RedisContainerDefinition containerDefinition) {
        this.containerDefinition = containerDefinition;
    }

    synchronized public RedisContainer getContainer(String imageName) {
        if (container != null) {
            return container;
        }
        container = createContainer(containerDefinition.containerName());
        container.start();
        return container;
    }

    public void clear() {
        if (container != null) {
            RedisURI uri = RedisURI.Builder
                .redis(container.getHost(), container.getMappedPort(REDIS_PORT))
                .build();
            try (RedisClient client = RedisClient.create(uri)) {
                StatefulRedisConnection<String, String> connection = client.connect();
                RedisCommands<String, String> commands = connection.sync();
                commands.flushall();
            }
        }
    }

    private RedisContainer createContainer(String imageName) {
        return new RedisContainer(DockerImageName.parse(imageName))
            .withLabel("com.testcontainers.desktop.service", "component-tests-redis")
            .withNetwork(Docker.network)
            .withExposedPorts(REDIS_PORT)
            .withReuse(true);
    }

}
