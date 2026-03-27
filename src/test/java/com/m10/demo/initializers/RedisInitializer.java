package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.integration.test.infrastructure.redis.RedisContainerDefinition;
import com.m10.integration.test.infrastructure.redis.RedisContextCustomizerFactory;
import com.m10.integration.test.support.Images;

public class RedisInitializer extends RedisContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new RedisContainerDefinition("redis", Images.REDIS_IMAGE);
        new RedisContextCustomizerFactory.RedisContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
