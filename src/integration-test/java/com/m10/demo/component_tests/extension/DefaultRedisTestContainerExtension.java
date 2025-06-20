package com.m10.demo.component_tests.extension;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DefaultRedisTestContainerExtension implements BeforeAllCallback, AfterEachCallback {

    public static final int REDIS_PORT = 6379;
    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse(Images.REDIS_IMAGE))
                    .withExposedPorts(REDIS_PORT)
                    .withReuse(true)
            ;

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            redisContainer.start();
            System.setProperty("spring.data.redis.host", redisContainer.getHost());
            System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(REDIS_PORT).toString());
            started = true;
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RedisURI uri = RedisURI.Builder
                .redis(redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT))
                .build();
        try (RedisClient client = RedisClient.create(uri)) {
            StatefulRedisConnection<String, String> connection = client.connect();
            RedisCommands<String, String> commands = connection.sync();
            commands.flushall();
        }
    }

    public static GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }

}
