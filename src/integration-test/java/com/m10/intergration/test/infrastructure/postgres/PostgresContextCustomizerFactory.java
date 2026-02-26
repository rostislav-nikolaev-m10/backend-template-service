package com.m10.intergration.test.infrastructure.postgres;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
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

import static com.m10.intergration.test.infrastructure.BeanDefinitionRegistryHelper.getBeanDefinitionRegistry;

@Slf4j
public class PostgresContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configurePostgresAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigurePostgresContainer.class);
        if (configurePostgresAnnotation != null) {
            var containerDefinition = new PostgresContainerDefinition(configurePostgresAnnotation.instanceName(), configurePostgresAnnotation.containerName());
            return new PostgresContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    protected static class PostgresContextCustomizer implements ContextCustomizer {

        private final PostgresContainerDefinition containerDefinition;

        public PostgresContextCustomizer(PostgresContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(PostgresContainerRegistrar.class);
            registrarDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(PostgresContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(PostgresContainerManager.class);
            registry.registerBeanDefinition(PostgresContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PostgresContextCustomizer that = (PostgresContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    protected static class PostgresContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        protected static final String BEAN_NAME = PostgresContainerRegistrar.class.getName();

        private final PostgresContainerDefinition containerDefinition;

        public PostgresContainerRegistrar(PostgresContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("PostgreSQL Container Auto-configuration requires ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(PostgresContainerManager.BEAN_NAME, PostgresContainerManager.class);
            var container = containerManager.getContainer(containerDefinition.containerName());
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MapPropertySource propertySource = new MapPropertySource(
                "postgresContainerProperties",
                Map.of(
                    String.format("testcontainers.%s.%s", containerDefinition.instanceName(), "jdbc-url"), container.getJdbcUrl(),
                    String.format("testcontainers.%s.%s", containerDefinition.instanceName(), "username"), container.getUsername(),
                    String.format("testcontainers.%s.%s", containerDefinition.instanceName(), "password"), container.getPassword(),
                    String.format("testcontainers.%s.%s", containerDefinition.instanceName(), "driver-class-name"), container.getDriverClassName()
                )
            );
            environment.getPropertySources().addFirst(propertySource);
            log.info(">>> PostgreSQL Container properties added to environment: <<<");
            Arrays.stream(propertySource.getPropertyNames()).forEach(propName ->
                log.info(">>> {}={}", propName, propertySource.getProperty(propName))
            );
            log.info(">>> <<<");
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
