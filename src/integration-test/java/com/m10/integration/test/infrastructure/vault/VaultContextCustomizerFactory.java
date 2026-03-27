package com.m10.integration.test.infrastructure.vault;

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

public class VaultContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configureVaultAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigureVaultContainer.class);
        if (configureVaultAnnotation != null) {
            var containerDefinition = new VaultContainerDefinition(configureVaultAnnotation.containerName());
            return new VaultContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    public static void customizeContextForDefinition(
        ConfigurableApplicationContext ctx,
        VaultContainerDefinition def
    ) {
        new VaultContextCustomizer(def).customizeContext(ctx);
    }

    protected static class VaultContextCustomizer implements ContextCustomizer {

        private final VaultContainerDefinition containerDefinition;

        public VaultContextCustomizer(VaultContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(VaultContainerRegistrar.class);
            registrarDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(VaultContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(VaultContainerManager.class);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(VaultContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            VaultContextCustomizer that = (VaultContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class VaultContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = VaultContainerRegistrar.class.getName();

        private final VaultContainerDefinition containerDefinition;

        public VaultContainerRegistrar(VaultContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("Vault Container Auto-configuration requires ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(VaultContainerManager.BEAN_NAME, VaultContainerManager.class);
            var container = containerManager.getContainer(containerDefinition.containerName());
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MapPropertySource propertySource = new MapPropertySource(
                "vaultContainerProperties",
                Map.of(
                    "vault.uri", container.getHttpHostAddress(),
                    "vault.authentication", "TOKEN",
                    "vault.token", "test",
                    "vault.engine-path", "transit/test-engine",
                    "vault.transit-key-name", "test-key"
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
