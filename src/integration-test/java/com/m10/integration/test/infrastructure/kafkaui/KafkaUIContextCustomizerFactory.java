package com.m10.integration.test.infrastructure.kafkaui;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;

import static com.m10.integration.test.infrastructure.BeanDefinitionRegistryHelper.getBeanDefinitionRegistry;


public class KafkaUIContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configureKafkaAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigureKafkaUIContainer.class);
        if (configureKafkaAnnotation != null) {
            var containerDefinition = new KafkaUIContainerDefinition(configureKafkaAnnotation.containerName());
            return new KafkaUIContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    public static void customizeContextForDefinition(
        ConfigurableApplicationContext ctx,
        KafkaUIContainerDefinition def
    ) {
        new KafkaUIContextCustomizer(def).customizeContext(ctx);
    }

    protected static class KafkaUIContextCustomizer implements ContextCustomizer {

        private final KafkaUIContainerDefinition containerDefinition;

        public KafkaUIContextCustomizer(KafkaUIContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(KafkaUIContainerRegistrar.class);
            registry.registerBeanDefinition(KafkaUIContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(KafkaUIContainerManager.class);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(KafkaUIContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            KafkaUIContextCustomizer that = (KafkaUIContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class KafkaUIContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = KafkaUIContainerRegistrar.class.getName();

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("Kafka Container Auto-configuration требует ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(KafkaUIContainerManager.BEAN_NAME, KafkaUIContainerManager.class);
            containerManager.getContainer();
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            // nothing to do
        }

        @Override
        public int getOrder() {
            return PriorityOrdered.HIGHEST_PRECEDENCE;
        }

    }

}
