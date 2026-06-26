package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.testkit.redis.RedisContainerDefinition;
import com.m10.testkit.redis.RedisContextCustomizerFactory;
import com.m10.integration.test.support.Images;


public class RedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new RedisContainerDefinition("redis", Images.REDIS_IMAGE);
        RedisContextCustomizerFactory.customizeContextForDefinition(context, containerDefinition);
    }

}
