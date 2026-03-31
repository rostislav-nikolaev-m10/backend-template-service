package com.m10.integration.test.support;

import org.springframework.boot.test.context.SpringBootTest;

import com.m10.demo.Application;
import com.m10.integration.test.infrastructure.kafka.AutoConfigureKafkaContainer;
import com.m10.integration.test.infrastructure.kafkaui.AutoConfigureKafkaUIContainer;
import com.m10.integration.test.infrastructure.postgres.AutoConfigurePostgresContainer;
import com.m10.integration.test.infrastructure.redis.AutoConfigureRedisContainer;
import com.m10.integration.test.infrastructure.redisui.AutoConfigureRedisUIContainer;
import com.m10.integration.test.infrastructure.vault.AutoConfigureVaultContainer;
import com.m10.integration.test.infrastructure.wiremock.AutoConfigureWireMockContainer;


@SpringBootTest(classes = Application.class)
@AutoConfigureKafkaContainer(containerName = Images.KAFKA_IMAGE)
@AutoConfigureKafkaUIContainer(containerName = Images.KAFKA_UI_IMAGE)
@AutoConfigurePostgresContainer(containerName = Images.POSTGRES_IMAGE)
@AutoConfigureRedisContainer(containerName = Images.REDIS_IMAGE)
@AutoConfigureRedisUIContainer(containerName = Images.REDIS_UI_IMAGE)
@AutoConfigureVaultContainer(containerName = Images.VAULT_IMAGE)
@AutoConfigureWireMockContainer(containerName = Images.WIREMOCK_IMAGE)
public abstract class CommonIT {

}
