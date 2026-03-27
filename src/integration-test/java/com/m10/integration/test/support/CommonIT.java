package com.m10.integration.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.m10.integration.test.infrastructure.kafka.AutoConfigureKafkaContainer;
import com.m10.integration.test.infrastructure.kafkaui.AutoConfigureKafkaUIContainer;
import com.m10.integration.test.infrastructure.postgres.AutoConfigurePostgresContainer;
import com.m10.integration.test.infrastructure.redis.AutoConfigureRedisContainer;
import com.m10.integration.test.infrastructure.vault.AutoConfigureVaultContainer;
import com.m10.integration.test.infrastructure.wiremock.AutoConfigureWireMockContainer;


@SpringBootTest
@AutoConfigureKafkaContainer
@AutoConfigureKafkaUIContainer
@AutoConfigurePostgresContainer
@AutoConfigureRedisContainer
@AutoConfigureVaultContainer
@AutoConfigureWireMockContainer
public abstract class CommonIT {

    @Autowired
    protected ObjectMapper objectMapper;

}
