package com.m10.testkit.postgres;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class PostgresTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(PostgresContainerManager.class)
            .ifAvailable(PostgresContainerManager::createSnapshot);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(PostgresContainerManager.class)
            .ifAvailable(PostgresContainerManager::restoreSnapshot);
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(PostgresContainerManager.class)
            .ifAvailable(PostgresContainerManager::restoreSnapshot);
    }

}
