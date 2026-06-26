package com.m10.testkit.wiremock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматической настройки контейнера WireMock в интеграционных тестах.
 *
 * <p>Эта аннотация используется для автоматической конфигурации и запуска контейнера WireMock
 * с помощью TestContainers во время выполнения интеграционных тестов. Аннотация наследуется
 * дочерними классами и применяется на уровне класса.</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @AutoConfigureWireMockContainer(containerName = "wiremock/wiremock:latest")
 * @SpringBootTest
 * class MyIntegrationTest {
 *     // тестовые методы
 * }
 * }
 * </pre>
 *
 * @see WireMockContainerManager
 * @see WireMockContextCustomizerFactory
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigureWireMockContainer {

    String containerName() default "";

}
