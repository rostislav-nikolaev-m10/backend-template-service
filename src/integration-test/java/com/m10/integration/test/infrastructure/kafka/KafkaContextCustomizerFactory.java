package com.m10.integration.test.infrastructure.kafka;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;

import static com.m10.integration.test.infrastructure.BeanDefinitionRegistryHelper.getBeanDefinitionRegistry;


public class KafkaContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configureKafkaAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigureKafkaContainer.class);
        if (configureKafkaAnnotation != null) {
            var containerDefinition = new KafkaContainerDefinition(configureKafkaAnnotation.containerName(), configureKafkaAnnotation.initialTopics());
            return new KafkaContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    public static void customizeContextForDefinition(
        ConfigurableApplicationContext ctx,
        KafkaContainerDefinition def
    ) {
        new KafkaContextCustomizer(def).customizeContext(ctx);
    }

    protected static class KafkaContextCustomizer implements ContextCustomizer {

        private final KafkaContainerDefinition containerDefinition;

        public KafkaContextCustomizer(KafkaContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(KafkaContainerRegistrar.class);
            registry.registerBeanDefinition(KafkaContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(KafkaContainerManager.class);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(KafkaContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            KafkaContextCustomizer that = (KafkaContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class KafkaContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = KafkaContainerRegistrar.class.getName();

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("Kafka Container Auto-configuration требует ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(KafkaContainerManager.BEAN_NAME, KafkaContainerManager.class);
            var container = containerManager.getContainer();
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MapPropertySource propertySource = new MapPropertySource(
                "kafkaContainerProperties",
                Map.of(
                    "spring.kafka.bootstrap-servers", container.getBootstrapServers(),
                    "spring.kafka.producer.bootstrap-servers", container.getBootstrapServers()
                )
            );
            environment.getPropertySources().addFirst(propertySource);
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
