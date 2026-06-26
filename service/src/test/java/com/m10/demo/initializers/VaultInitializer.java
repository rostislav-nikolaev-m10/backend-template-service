package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.testkit.vault.VaultContainerDefinition;
import com.m10.testkit.vault.VaultContextCustomizerFactory;
import com.m10.integration.test.support.Images;


public class VaultInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new VaultContainerDefinition(Images.VAULT_IMAGE);
        VaultContextCustomizerFactory.customizeContextForDefinition(context, containerDefinition);
    }

}
