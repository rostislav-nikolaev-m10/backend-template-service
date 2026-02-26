package com.m10.intergration.test.infrastructure.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.m10.intergration.test.infrastructure.Docker;


/**
 * Менеджер контейнера WireMock для интеграционных тестов.
 *
 * <p>Этот класс управляет жизненным циклом контейнера WireMock, включая его создание,
 * запуск и предоставление доступа к клиенту WireMock. Реализует паттерн singleton
 * для обеспечения единственного экземпляра контейнера на протяжении выполнения тестов.</p>
 *
 * <p>Класс обеспечивает:</p>
 * <ul>
 *   <li>Ленивую инициализацию контейнера WireMock</li>
 *   <li>Thread-safe доступ к контейнеру</li>
 *   <li>Автоматическое создание клиента WireMock</li>
 *   <li>Сброс настроек маппингов между тестами</li>
 * </ul>
 *
 * @see WireMockContainerDefinition
 * @see AutoConfigureWireMockContainer
 */
public class WireMockContainerManager {

    /**
     * Имя bean'а для регистрации в Spring контексте.
     */
    public static final String BEAN_NAME = WireMockContainerManager.class.getName();

    /**
     * Порт, на котором WireMock слушает входящие запросы внутри контейнера.
     */
    public static final int WIREMOCK_PORT = 8080;

    private GenericContainer<?> container = null;
    private final WireMockContainerDefinition wireMockContainerDefinition;

    private WireMock wireMockClient;

    /**
     * Создает новый экземпляр менеджера контейнера WireMock.
     *
     * @param wireMockContainerDefinition конфигурация контейнера WireMock
     * @throws IllegalArgumentException если definition равен null
     */
    public WireMockContainerManager(WireMockContainerDefinition wireMockContainerDefinition) {
        this.wireMockContainerDefinition = wireMockContainerDefinition;
    }

    /**
     * Получает экземпляр контейнера WireMock.
     *
     * <p>Если контейнер еще не создан, выполняет ленивую инициализацию:
     * создает контейнер, запускает его и инициализирует клиент WireMock.</p>
     *
     * @return экземпляр запущенного контейнера WireMock
     */
    synchronized public GenericContainer<?> getContainer() {
        if (container != null) {
            return container;
        }
        GenericContainer<?> newContainer = createContainer(wireMockContainerDefinition.containerName());
        newContainer.start();
        wireMockClient = new WireMock(newContainer.getHost(), newContainer.getMappedPort(WIREMOCK_PORT));
        container = newContainer;
        return container;
    }

    /**
     * Получает клиент WireMock для взаимодействия с сервером.
     *
     * <p>Если контейнер еще не инициализирован, автоматически вызывает
     * {@link #getContainer()} для создания и запуска контейнера.</p>
     *
     * @return клиент WireMock для выполнения операций с mock-сервером
     */
    public WireMock getWireMockClient() {
        if (wireMockClient == null) {
            getContainer();
        }
        return wireMockClient;
    }

    /**
     * Сбрасывает все настроенные маппинги в WireMock.
     *
     * <p>Этот метод используется для очистки состояния WireMock между тестами,
     * обеспечивая изоляцию тестов. Выполняется синхронно.</p>
     */
    synchronized public void resetMappings() {
        if (wireMockClient != null && container != null && container.isRunning()) {
            wireMockClient.resetMappings();
        }
    }

    /**
     * Создает новый контейнер WireMock с заданным Docker-образом.
     *
     * <p>Контейнер настраивается с:</p>
     * <ul>
     *   <li>Проброшенным портом {@value #WIREMOCK_PORT}</li>
     *   <li>Меткой для идентификации сервиса</li>
     *   <li>Подключением к общей сети Docker</li>
     *   <li>Включенным режимом переиспользования</li>
     * </ul>
     *
     * @param imageName имя Docker-образа для создания контейнера
     * @return настроенный, но не запущенный контейнер
     */
    private GenericContainer<?> createContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
            .withExposedPorts(WIREMOCK_PORT)
            .withLabel("com.testcontainers.desktop.service", "component-tests-wiremock")
            .withNetwork(Docker.network)
            .withReuse(true);
    }

}
