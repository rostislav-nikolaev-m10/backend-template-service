package com.m10.intergration.test.infrastructure.wiremock;

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

import static com.m10.intergration.test.infrastructure.BeanDefinitionRegistryHelper.getBeanDefinitionRegistry;
import static com.m10.intergration.test.infrastructure.wiremock.WireMockContainerManager.WIREMOCK_PORT;


/**
 * Фабрика для создания кастомизатора контекста WireMock.
 *
 * <p>Этот класс реализует паттерн Factory для создания {@link ContextCustomizer},
 * который автоматически настраивает Spring контекст для работы с контейнером WireMock
 * в интеграционных тестах. Фабрика активируется при наличии аннотации
 * {@link AutoConfigureWireMockContainer} на тестовом классе.</p>
 *
 * <p>Основные функции:</p>
 * <ul>
 *   <li>Обнаружение аннотации {@link AutoConfigureWireMockContainer}</li>
 *   <li>Создание соответствующего кастомизатора контекста</li>
 *   <li>Автоматическая регистрация необходимых bean'ов в Spring контексте</li>
 * </ul>
 *
 * @see AutoConfigureWireMockContainer
 * @see WireMockContainerManager
 * @see ContextCustomizerFactory
 */
public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {

    /**
     * Создает кастомизатор контекста для настройки WireMock.
     *
     * <p>Проверяет наличие аннотации {@link AutoConfigureWireMockContainer} на тестовом классе.
     * Если аннотация найдена, создает и возвращает соответствующий кастомизатор контекста.</p>
     *
     * @param testClass        класс теста для проверки аннотаций
     * @param configAttributes атрибуты конфигурации контекста (не используется)
     * @return кастомизатор контекста WireMock или null, если аннотация не найдена
     */
    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        var configureWireMockAnnotation =
            TestContextAnnotationUtils.findMergedAnnotation(testClass, AutoConfigureWireMockContainer.class);
        if (configureWireMockAnnotation != null) {
            var containerDefinition = new WireMockContainerDefinition(configureWireMockAnnotation.containerName());
            return new WireMockContextCustomizer(containerDefinition);
        } else {
            return null;
        }
    }

    /**
     * Кастомизатор контекста для настройки WireMock контейнера.
     *
     * <p>Этот внутренний класс отвечает за регистрацию необходимых bean'ов
     * в Spring контексте для работы с контейнером WireMock. Регистрирует
     * {@link WireMockContainerManager} и {@link WireMockContainerRegistrar}.</p>
     *
     * @see ContextCustomizer
     */
    protected static class WireMockContextCustomizer implements ContextCustomizer {

        private final WireMockContainerDefinition containerDefinition;

        /**
         * Создает новый кастомизатор контекста с заданной конфигурацией контейнера.
         *
         * @param containerDefinition конфигурация контейнера WireMock
         */
        public WireMockContextCustomizer(WireMockContainerDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }

        /**
         * Настраивает Spring контекст для работы с WireMock.
         *
         * <p>Регистрирует следующие bean'ы:</p>
         * <ul>
         *   <li>{@link WireMockContainerRegistrar} - для post-processing регистрации</li>
         *   <li>{@link WireMockContainerManager} - для управления контейнером</li>
         * </ul>
         *
         * @param context      настраиваемый контекст приложения
         * @param mergedConfig объединенная конфигурация контекста
         */
        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            customizeContext(context);
        }

        public void customizeContext(ConfigurableApplicationContext context) {
            var registry = getBeanDefinitionRegistry(context);

            var registrarDefinition = new RootBeanDefinition(WireMockContainerRegistrar.class);
            registry.registerBeanDefinition(WireMockContainerRegistrar.BEAN_NAME, registrarDefinition);

            var containerFactoryDefinition = new RootBeanDefinition(WireMockContainerManager.class);
            containerFactoryDefinition.getConstructorArgumentValues()
                .addIndexedArgumentValue(0, containerDefinition);
            registry.registerBeanDefinition(WireMockContainerManager.BEAN_NAME, containerFactoryDefinition);
        }

        /**
         * Необходимо для отслеживания изменений контекста между тестовыми классами.
         * Если конфигурация контейнера не менялась, то из-за этого кастомайзера контекст пересоздаваться не будет,
         * будет переиспользован из кэша.
         */
        @Override
        public int hashCode() {
            return containerDefinition.hashCode();
        }

        /**
         * Необходимо для отслеживания изменений контекста между тестовыми классами.
         * Если конфигурация контейнера не менялась, то из-за этого кастомайзера контекст пересоздаваться не будет,
         * будет переиспользован из кэша.
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            WireMockContextCustomizer that = (WireMockContextCustomizer) o;
            return Objects.equals(containerDefinition, that.containerDefinition);
        }

    }

    /**
     * Post-processor для регистрации контейнера WireMock в Spring контексте.
     *
     * <p>Этот класс выполняет финальную настройку контейнера WireMock после
     * инициализации bean'ов. Запускает контейнер и добавляет свойства
     * окружения с URL для подключения к WireMock серверу.</p>
     *
     * <p>Имеет высший приоритет выполнения для обеспечения раннего запуска контейнера.</p>
     *
     * @see BeanDefinitionRegistryPostProcessor
     * @see PriorityOrdered
     */
    protected static class WireMockContainerRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        /**
         * Имя bean'а для регистрации в Spring контексте.
         */
        protected static final String BEAN_NAME = WireMockContainerRegistrar.class.getName();

        /**
         * Выполняет post-processing регистрации bean'ов.
         *
         * <p>Получает менеджер контейнера, запускает контейнер WireMock
         * и добавляет свойство {@code wiremock.base-url} в окружение
         * для использования в тестах.</p>
         *
         * @param registry реестр определений bean'ов
         * @throws IllegalStateException если фабрика bean'ов не является ConfigurableListableBeanFactory
         */
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
                throw new IllegalStateException("WireMock Container Auto-configuration requires ConfigurableListableBeanFactory");
            }
            var containerManager = beanFactory.getBean(WireMockContainerManager.BEAN_NAME, WireMockContainerManager.class);
            var container = containerManager.getContainer();
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            MapPropertySource propertySource = new MapPropertySource(
                "wireMockContainerProperties",
                Map.of(
                    "wiremock.base-url", "http://%s:%s".formatted(container.getHost(), container.getMappedPort(WIREMOCK_PORT))
                )
            );
            environment.getPropertySources().addFirst(propertySource);
        }

        /**
         * Выполняет post-processing фабрики bean'ов.
         *
         * <p>В данной реализации не выполняет никаких действий.</p>
         *
         * @param beanFactory фабрика bean'ов для обработки
         */
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            // nothing to do
        }

        /**
         * Возвращает порядок выполнения post-processor'а.
         *
         * <p>Использует высший приоритет для обеспечения раннего выполнения
         * и доступности контейнера WireMock для других компонентов.</p>
         *
         * @return {@link PriorityOrdered#HIGHEST_PRECEDENCE}
         */
        @Override
        public int getOrder() {
            return PriorityOrdered.HIGHEST_PRECEDENCE;
        }

    }

}
