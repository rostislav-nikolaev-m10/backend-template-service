package com.m10.integration.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.m10.demo.Application;
import com.m10.testkit.kafka.AutoConfigureKafkaContainer;
import com.m10.testkit.kafkaui.AutoConfigureKafkaUIContainer;
import com.m10.testkit.postgres.AutoConfigurePostgresContainer;
import com.m10.testkit.redis.AutoConfigureRedisContainer;
import com.m10.testkit.redisui.AutoConfigureRedisUIContainer;
import com.m10.testkit.vault.AutoConfigureVaultContainer;
import com.m10.testkit.wiremock.AutoConfigureWireMockContainer;


@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureKafkaContainer(containerName = Images.KAFKA_IMAGE)
@AutoConfigurePostgresContainer(containerName = Images.POSTGRES_IMAGE)
@AutoConfigureRedisContainer(containerName = Images.REDIS_IMAGE)
@AutoConfigureVaultContainer(containerName = Images.VAULT_IMAGE)
@AutoConfigureWireMockContainer(containerName = Images.WIREMOCK_IMAGE)
@AutoConfigureKafkaUIContainer(containerName = Images.KAFKA_UI_IMAGE)
@AutoConfigureRedisUIContainer(containerName = Images.REDIS_UI_IMAGE)
public abstract class CommonIT {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    protected String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
