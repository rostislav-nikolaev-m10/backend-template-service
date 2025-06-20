package com.m10.demo.component_tests.extension;

import com.m10.demo.component_tests.helpers.VaultTestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

public class DefaultVaultTestContainerExtension implements BeforeAllCallback, AfterEachCallback {

    public static final String VAULT_TOKEN = "test";
    public static final int VAULT_PORT = 8200;

    private static final VaultContainer<?> vaultContainer =
            new VaultContainer<>(DockerImageName.parse(Images.VAULT_IMAGE))
                    .withVaultToken(VAULT_TOKEN)
                    .withLabel("com.testcontainers.desktop.service", "component-tests-vault")
                    .withReuse(true)
            ;

    private static boolean started = false;

    @SneakyThrows
    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            vaultContainer.start();
            vaultContainer.execInContainer("vault",  "secrets", "enable", "-path=transit", "transit");
            VaultTestUtils.addVaultPolicy(vaultContainer);
            System.setProperty("vault.uri", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(VAULT_PORT));
            System.setProperty("vault.token", VAULT_TOKEN);
            started = true;
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        VaultTestUtils.flushVault(vaultContainer);
    }

    public static VaultContainer<?> getVaultContainer() {
        return vaultContainer;
    }

}
