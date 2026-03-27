package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.integration.test.infrastructure.kafkaui.KafkaUIContainerDefinition;
import com.m10.integration.test.infrastructure.kafkaui.KafkaUIContextCustomizerFactory;
import com.m10.integration.test.support.Images;

public class KafkaUIInitializer extends KafkaUIContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new KafkaUIContainerDefinition(Images.KAFKA_UI_IMAGE);
        new KafkaUIContextCustomizerFactory.KafkaUIContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
