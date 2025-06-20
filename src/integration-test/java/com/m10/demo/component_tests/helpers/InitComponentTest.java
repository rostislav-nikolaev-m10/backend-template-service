package com.m10.demo.component_tests.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.m10.demo.component_tests.extension.SpringKafkaContextInitializer;
import com.m10.demo.component_tests.extension.SpringRedisContextInitializer;
import com.m10.demo.component_tests.extension.SpringPostgresContextInitializer;
import com.m10.demo.component_tests.extension.SpringWireMockContextInitializer;
import com.m10.demo.component_tests.extension.UseKafkaTestcontainer;
import com.m10.demo.component_tests.extension.UsePostgresTestcontainer;
import com.m10.demo.component_tests.extension.UseRedisTestcontainer;
import com.m10.demo.component_tests.extension.UseWireMockTestcontainer;
import com.m10.demo.component_tests.extension.UseVaultTestcontainer;
import org.springframework.test.web.servlet.MockMvc;

//@UseKafkaTestcontainer
@UseVaultTestcontainer
@UsePostgresTestcontainer
//@UseRedisTestcontainer
//@UseWireMockTestcontainer
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
        SpringPostgresContextInitializer.class,
//        SpringWireMockContextInitializer.class,
//        SpringKafkaContextInitializer.class,
//        SpringRedisContextInitializer.class
})
@ActiveProfiles({"test"})
public abstract class InitComponentTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    // fixme вынести в отдельный objectMapper для данных, передающихся в mockMvc (маппер тестовых данных не должен совпадать с маппером приложения, иначе можно пропустить ошибки в конфигурации маппера)
    @SneakyThrows
    protected String toJson(Object request) {
        return objectMapper.writeValueAsString(request);
    }

}
