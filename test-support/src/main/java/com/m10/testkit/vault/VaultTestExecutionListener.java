package com.m10.testkit.vault;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;


public class VaultTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void afterTestMethod(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(VaultContainerManager.class)
            .ifAvailable(VaultContainerManager::flushVault);
    }

}
