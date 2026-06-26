package com.m10.demo;

import org.springframework.boot.builder.SpringApplicationBuilder;

import com.m10.demo.initializers.KafkaInitializer;
import com.m10.demo.initializers.KafkaUIInitializer;
import com.m10.demo.initializers.PostgresInitializer;
import com.m10.demo.initializers.RedisInitializer;
import com.m10.demo.initializers.VaultInitializer;
import com.m10.demo.initializers.WireMockInitializer;

public class TestApplication {

    public static void main(String[] args) {
        System.setProperty("app.test.mode", "true");
        new SpringApplicationBuilder()
            .sources(Application.class)
            .initializers(
                new KafkaInitializer(),
                new KafkaUIInitializer(),
                new PostgresInitializer(),
                new RedisInitializer(),
                new VaultInitializer(),
                new WireMockInitializer()
            )
            .run(args);
    }

}
