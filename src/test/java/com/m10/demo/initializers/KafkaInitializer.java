package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.intergration.test.infrastructure.kafka.KafkaContainerDefinition;
import com.m10.intergration.test.infrastructure.kafka.KafkaContextCustomizerFactory;
import com.m10.intergration.test.support.Images;

public class KafkaInitializer extends KafkaContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new KafkaContainerDefinition(Images.KAFKA_IMAGE, new String[]{});
        new KafkaContextCustomizerFactory.KafkaContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
