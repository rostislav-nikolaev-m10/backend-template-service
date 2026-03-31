package com.m10.integration.test.infrastructure.redisui;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
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

import com.m10.integration.test.infrastructure.redis.RedisContainerManager;

import static com.m10.integration.test.infrastructure.BeanDefinitionRegistryHelper.getBeanDefinitionRegistry;

public class RedisUIContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
                                                     List<ContextConfigurationAttributes> configAttributes) {
        var configureRedisAnnotation = TestContextAnnotationUtils.findMergedAnnotation(testClass,
            AutoConfigureRedisUIContainer.class);
        if (configureRedisAnnotation != null) {
            var containerDefinition = new RedisUIContainerDefinition(configureRedisAnnotation.instanceName(),
                configureRedisAnnotation.containerName());
            return new RedisUIContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    public static void customizeContextForDefinition(
        ConfigurableApplicationContext ctx,
        RedisUIContainerDefinition def) {
        new RedisUIContextCustomizer(def).customizeContext(ctx);
    }

    protected static class RedisUIContextCustomizer implements ContextCustomizer {

        private final RedisUIContainerDefinition containerDefinition;

        public RedisUIContextCustomizer(RedisUIContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(RedisUIContainerRegistrar.class);
            registry.registerBeanDefinition(RedisUIContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(RedisUIContainerManager.class);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(1, new RuntimeBeanReference(RedisContainerManager.BEAN_NAME));
            registry.registerBeanDefinition(RedisUIContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            RedisUIContextCustomizer that = (RedisUIContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class RedisUIContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = RedisUIContainerRegistrar.class.getName();

        public RedisUIContainerRegistrar() {
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException(
                    "RedisUI Container Auto-configuration requires ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(RedisUIContainerManager.BEAN_NAME,
                RedisUIContainerManager.class);
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
