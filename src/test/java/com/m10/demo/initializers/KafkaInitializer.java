package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.integration.test.infrastructure.kafka.KafkaContainerDefinition;
import com.m10.integration.test.infrastructure.kafka.KafkaContextCustomizerFactory;
import com.m10.integration.test.support.Images;


public class KafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new KafkaContainerDefinition(Images.KAFKA_IMAGE, new String[]{});
        KafkaContextCustomizerFactory.customizeContextForDefinition(context, containerDefinition);
    }

}
