package com.m10.integration.test.infrastructure.wiremock;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Слушатель выполнения тестов для управления состоянием WireMock контейнера.
 *
 * <p>Этот класс отвечает за автоматическую очистку маппингов WireMock между тестами,
 * обеспечивая изоляцию тестов и предотвращая влияние одного теста на другой.
 * Наследует от {@link AbstractTestExecutionListener} и переопределяет ключевые
 * методы жизненного цикла выполнения тестов.</p>
 *
 * <p>Основные функции:</p>
 * <ul>
 *   <li>Очистка маппингов WireMock перед каждым тестовым методом</li>
 * </ul>
 *
 * <p>Использование:</p>
 * <p>Данный слушатель автоматически активируется при использовании аннотации
 * {@link AutoConfigureWireMockContainer} и не требует явной регистрации.</p>
 *
 * @see AbstractTestExecutionListener
 * @see WireMockContainerManager
 * @see AutoConfigureWireMockContainer
 */
public class WireMockTestExecutionListener extends AbstractTestExecutionListener {

    /**
     * Выполняется перед каждым тестовым методом.
     *
     * <p>Сбрасывает все настроенные маппинги в WireMock для обеспечения
     * изоляции между отдельными тестовыми методами. Это гарантирует, что
     * каждый тест начинается с чистого состояния WireMock.</p>
     *
     * @param testContext контекст выполнения теста
     */
    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext().getBeanProvider(WireMockContainerManager.class)
            .ifAvailable(WireMockContainerManager::resetMappings);
        testContext.getApplicationContext().getBeanProvider(WireMockContainerManager.class)
            .ifAvailable(WireMockContainerManager::resetMappings);
    }

}
