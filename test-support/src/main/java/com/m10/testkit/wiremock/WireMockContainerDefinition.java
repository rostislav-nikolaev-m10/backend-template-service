package com.m10.testkit.wiremock;

/**
 * Record-класс для определения конфигурации контейнера WireMock.
 *
 * <p>Этот immutable record содержит конфигурационные параметры для создания
 * и управления контейнером WireMock в интеграционных тестах. Используется
 * совместно с {@link WireMockContainerManager} для инициализации контейнера.</p>
 *
 * @param containerName имя Docker-образа для контейнера WireMock
 * @see WireMockContainerManager
 * @see AutoConfigureWireMockContainer
 */
public record WireMockContainerDefinition(
    String containerName
) {
}
