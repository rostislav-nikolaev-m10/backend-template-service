package com.m10.demo.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.m10.intergration.test.support.Images;
import com.m10.intergration.test.infrastructure.vault.VaultContainerDefinition;
import com.m10.intergration.test.infrastructure.vault.VaultContextCustomizerFactory;


public class VaultInitializer extends VaultContextCustomizerFactory
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var containerDefinition = new VaultContainerDefinition(Images.VAULT_IMAGE);
        new VaultContextCustomizerFactory.VaultContextCustomizer(containerDefinition)
            .customizeContext(context);
    }

}
