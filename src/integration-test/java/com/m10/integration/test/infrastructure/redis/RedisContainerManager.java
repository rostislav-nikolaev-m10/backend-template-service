package com.m10.integration.test.infrastructure.redis;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.concurrent.atomic.AtomicReference;
import org.testcontainers.utility.DockerImageName;

import com.m10.integration.test.infrastructure.Docker;


public class RedisContainerManager {

    public static final String BEAN_NAME = RedisContainerManager.class.getName();
    public static final int REDIS_PORT = 6379;

    private final AtomicReference<RedisContainer> container = new AtomicReference<>(null);
    private RedisClient redisClient;

    synchronized public RedisContainer getContainer(String imageName) {
        if (container.get() != null) {
            return container.get();
        }
        RedisContainer newContainer = createContainer(imageName);
        if (container.compareAndSet(null, newContainer)) {
            newContainer.start();
            return newContainer;
        } else {
            return container.get();
        }
    }

    public void clear() {
        if (container.get() != null) {
            RedisURI uri = RedisURI.Builder
                .redis(container.get().getHost(), container.get().getMappedPort(REDIS_PORT))
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
