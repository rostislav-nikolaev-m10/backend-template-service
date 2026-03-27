package com.m10.integration.test.infrastructure.redis;

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

public class RedisContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configureRedisAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigureRedisContainer.class);
        if (configureRedisAnnotation != null) {
            var containerDefinition = new RedisContainerDefinition(configureRedisAnnotation.instanceName(), configureRedisAnnotation.containerName());
            return new RedisContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    protected static class RedisContextCustomizer implements ContextCustomizer {

        private final RedisContainerDefinition containerDefinition;

        public RedisContextCustomizer(RedisContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(RedisContainerRegistrar.class);
            registrarDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(RedisContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(RedisContainerManager.class);
            registry.registerBeanDefinition(RedisContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RedisContextCustomizer that = (RedisContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class RedisContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = RedisContainerRegistrar.class.getName();

        private final RedisContainerDefinition containerDefinition;

        public RedisContainerRegistrar(RedisContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("Vault Container Auto-configuration requires ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(RedisContainerManager.BEAN_NAME, RedisContainerManager.class);
            var container = containerManager.getContainer(containerDefinition.containerName());
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MapPropertySource propertySource = new MapPropertySource(
                "redisContainerProperties",
                Map.of(
                    "spring.data.redis.host", container.getHost(),
                    "spring.data.redis.port", container.getMappedPort(RedisContainerManager.REDIS_PORT).toString()
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
