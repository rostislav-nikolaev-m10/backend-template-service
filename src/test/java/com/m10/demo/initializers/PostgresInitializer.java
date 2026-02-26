package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.intergration.test.support.Images;
import com.m10.intergration.test.infrastructure.postgres.PostgresContainerDefinition;
import com.m10.intergration.test.infrastructure.postgres.PostgresContextCustomizerFactory;


public class PostgresInitializer extends PostgresContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new PostgresContainerDefinition("postgres", Images.POSTGRES_IMAGE);
        new PostgresContextCustomizerFactory.PostgresContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
