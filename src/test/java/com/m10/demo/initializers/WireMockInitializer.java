package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.integration.test.support.Images;
import com.m10.integration.test.infrastructure.wiremock.WireMockContainerDefinition;
import com.m10.integration.test.infrastructure.wiremock.WireMockContextCustomizerFactory;


public class WireMockInitializer extends WireMockContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new WireMockContainerDefinition(Images.WIREMOCK_IMAGE);
        new WireMockContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
