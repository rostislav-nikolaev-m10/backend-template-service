package com.m10.integration.test.infrastructure.redis;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class RedistTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void afterTestMethod(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(RedisContainerManager.class)
            .ifAvailable(RedisContainerManager::clear);
    }

}
