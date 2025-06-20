package com.m10.demo.component_tests.extension;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;
import java.util.stream.Stream;

public class SpringKafkaContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        ctx.addApplicationListener((ApplicationListener<ApplicationReadyEvent>) event -> {
            var env = event.getApplicationContext().getEnvironment();
            DefaultKafkaTestContainerExtension.setTopicsToCreate(
                    Stream.of(
                                    env.getProperty("app.accounting.topic.w2w"),
                                    env.getProperty("app.accounting.topic.reserve-funds-w2w-callback"),
                                    env.getProperty("app.accounting.topic.apply-partial-reservation-w2w-callback"),
                                    env.getProperty("app.accounting.topic.revoke-partial-reservation-w2w-callback"),
                                    env.getProperty("app.accounting.topic.revoke-full-reservation-w2w-callback"),
                                    env.getProperty("app.history.topic")
                            )
                            .filter(Objects::nonNull)
                            .toList()
            );
            DefaultKafkaTestContainerExtension.createInitialTopics();
        });
    }

}
