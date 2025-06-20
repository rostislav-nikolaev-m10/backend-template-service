package com.m10.demo.component_tests.extension;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class SpringPostgresContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        var postgresContainer = DefaultPostgresTestContainerExtension.startContainer();
        var env = ctx.getEnvironment();
        env.getPropertySources().addFirst(
                new MapPropertySource(
                        "postgresContainerProperties",
                        Map.of(
                                "spring.datasource.url", postgresContainer.getJdbcUrl(),
                                "spring.datasource.username", postgresContainer.getUsername(),
                                "spring.datasource.password", postgresContainer.getPassword(),
                                "spring.liquibase.url", postgresContainer.getJdbcUrl(),
                                "spring.liquibase.user", postgresContainer.getUsername(),
                                "spring.liquibase.password", postgresContainer.getPassword()
                        )
                )
        );
        ctx.addApplicationListener((ApplicationListener<ApplicationReadyEvent>) event -> {
            DefaultPostgresTestContainerExtension.createSnapshot();
        });
    }

}
