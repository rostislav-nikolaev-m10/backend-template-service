package com.m10.integration.test.infrastructure.vault;

import org.testcontainers.vault.VaultContainer;

import com.m10.integration.test.infrastructure.Docker;


public class VaultContainerManager {

    public static final String BEAN_NAME = VaultContainerManager.class.getName();

    private final VaultContainerDefinition containerDefinition;

    private VaultContainer<?> container;

    public VaultContainerManager(VaultContainerDefinition containerDefinition) {
        this.containerDefinition = containerDefinition;
    }

    synchronized public VaultContainer<?> getContainer(String imageName) {
        if (container != null) {
            return container;
        }
        container = createContainer(containerDefinition.containerName());
        container.start();
        return container;
    }

    private VaultContainer<?> createContainer(String imageName) {
        return new VaultContainer<>(imageName)
            .withVaultToken("test")
            .withInitCommand(
                "secrets enable -path=transit/test-engine transit",
                "write -f transit/test-engine/keys/test-key"
            )
            .withLabel("com.testcontainers.desktop.service", "component-tests-vault")
            .withNetwork(Docker.network)
            .withReuse(true);
    }

}
