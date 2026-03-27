package com.m10.integration.test.infrastructure.kafka;

import org.springframework.context.Lifecycle;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class KafkaTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(KafkaContainerManager.class)
            .ifAvailable(KafkaContainerManager::createInitialTopics);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(KafkaContainerManager.class)
            .ifAvailable(KafkaContainerManager::resetKafkaTopicsOffsets);
    }

    @Override
    public void beforeTestExecution(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(KafkaListenerEndpointRegistry.class)
            .ifAvailable(registry -> {
                registry.getListenerContainers().forEach(container -> {
                    if (!container.isRunning()) {
                        container.start();
                    }
                    var topics = container.getContainerProperties().getTopics();
                    container.getGroupId();
                    ContainerTestUtils.waitForAssignment(container, topics.length);
                });
            });
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(KafkaListenerEndpointRegistry.class)
            .ifAvailable(registry -> {
                registry.getListenerContainers().forEach(Lifecycle::stop);
            });
        testContext.getApplicationContext().getBeanProvider(KafkaContainerManager.class)
            .ifAvailable(KafkaContainerManager::resetKafkaTopicsOffsets);
    }

}
