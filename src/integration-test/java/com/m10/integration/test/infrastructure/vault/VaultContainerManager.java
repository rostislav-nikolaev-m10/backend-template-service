package com.m10.integration.test.infrastructure.vault;

import java.util.concurrent.atomic.AtomicReference;
import org.testcontainers.vault.VaultContainer;

import com.m10.integration.test.infrastructure.Docker;


public class VaultContainerManager {

    public static final String BEAN_NAME = VaultContainerManager.class.getName();

    private final AtomicReference<VaultContainer<?>> container = new AtomicReference<>(null);

    synchronized public VaultContainer<?> getContainer(String imageName) {
        if (container.get() != null) {
            return container.get();
        }
        VaultContainer<?> newContainer = createContainer(imageName);
        if (container.compareAndSet(null, newContainer)) {
            newContainer.start();
            return newContainer;
        } else {
            return container.get();
        }
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
