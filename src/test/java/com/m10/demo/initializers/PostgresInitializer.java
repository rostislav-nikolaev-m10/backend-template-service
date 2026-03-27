package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.integration.test.infrastructure.postgres.PostgresContainerDefinition;
import com.m10.integration.test.infrastructure.postgres.PostgresContextCustomizerFactory;
import com.m10.integration.test.support.Images;


public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new PostgresContainerDefinition("postgres", Images.POSTGRES_IMAGE);
        PostgresContextCustomizerFactory.customizeContextForDefinition(context, containerDefinition);
    }

}
