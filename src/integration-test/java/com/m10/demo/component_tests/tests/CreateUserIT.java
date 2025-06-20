package com.m10.demo.component_tests.tests;

import com.jayway.jsonpath.JsonPath;
import com.m10.demo.component_tests.extension.DefaultVaultTestContainerExtension;
import com.m10.demo.component_tests.helpers.InitComponentTest;
import com.m10.demo.component_tests.helpers.VaultTestUtils;
import com.m10.demo.dto.CreateUserProfileRequest;
import com.m10.demo.service.UserProfileService;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.vault.VaultContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CreateUserIT extends InitComponentTest {

    @Value("${vault.encryption.key-name}")
    private String keyName;

    private VaultContainer<?> vaultContainer = DefaultVaultTestContainerExtension.getVaultContainer();

    @Autowired
    private UserProfileService userProfileService;

    @Nested
    class Success {

        @SneakyThrows
        @Test
        @DisplayName("Успешное создание профиля пользователя")
        void saveNewUser() {
            // given
            VaultTestUtils.createKey(keyName, vaultContainer);
            CreateUserProfileRequest createUserProfileRequest =
                    CreateUserProfileRequest.builder()
                            .username(UUID.randomUUID().toString())
                            .firstName("Ivan")
                            .lastName("Ivanov")
                            .build();
            // when
            ResultActions saveResult =
                    mockMvc.perform(
                            MockMvcRequestBuilders.post("/user-profiles")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(createUserProfileRequest))
                                    .accept(MediaType.APPLICATION_JSON)
                    );
            // then
            saveResult.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.username").value(createUserProfileRequest.getUsername()))
                    .andExpect(jsonPath("$.firstName").value(createUserProfileRequest.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(createUserProfileRequest.getLastName()));
        }

        @SneakyThrows
        @Test
        @DisplayName("Можем прочитать профиль пользователя после создания")
        void getNewUserAfterCreate() {
            // given
            VaultTestUtils.createKey(keyName, vaultContainer);
            CreateUserProfileRequest createUserProfileRequest =
                    CreateUserProfileRequest.builder()
                            .username(UUID.randomUUID().toString())
                            .firstName("Ivan")
                            .lastName("Ivanov")
                            .build();
            // when
            ResultActions saveResult =
                    mockMvc.perform(
                            MockMvcRequestBuilders.post("/user-profiles")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(createUserProfileRequest))
                                    .accept(MediaType.APPLICATION_JSON)
                    );
            String userId = JsonPath.read(saveResult.andReturn().getResponse().getContentAsString(), "$.id");
            ResultActions getResult =
                    mockMvc.perform(
                            MockMvcRequestBuilders.get("/user-profiles/{id}", userId)
                                    .accept(MediaType.APPLICATION_JSON)
                    );
            // then
            getResult.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.username").value(createUserProfileRequest.getUsername()))
                    .andExpect(jsonPath("$.firstName").value(createUserProfileRequest.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(createUserProfileRequest.getLastName()));
        }

        @SneakyThrows
        @Test
        @DisplayName("Можем прочитать профиль пользователя после создания и ротации ключа")
        void getNewUserAfterCreateAndKeyRotation() {
            // given
            VaultTestUtils.createKey(keyName, vaultContainer);
            CreateUserProfileRequest createUserProfileRequest =
                    CreateUserProfileRequest.builder()
                            .username(UUID.randomUUID().toString())
                            .firstName("Ivan")
                            .lastName("Ivanov")
                            .build();
            // when
            ResultActions saveResult =
                    mockMvc.perform(
                            MockMvcRequestBuilders.post("/user-profiles")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(createUserProfileRequest))
                                    .accept(MediaType.APPLICATION_JSON)
                    );
            VaultTestUtils.rotateKey(keyName, vaultContainer);
            String userId = JsonPath.read(saveResult.andReturn().getResponse().getContentAsString(), "$.id");
            ResultActions getResult =
                    mockMvc.perform(
                            MockMvcRequestBuilders.get("/user-profiles/{id}", userId)
                                    .accept(MediaType.APPLICATION_JSON)
                    );
            // then
            getResult.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.username").value(createUserProfileRequest.getUsername()))
                    .andExpect(jsonPath("$.firstName").value(createUserProfileRequest.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(createUserProfileRequest.getLastName()));
        }

        @SneakyThrows
        @Test
        @DisplayName("Можем найти профиль пользователя по имени")
        void findUserByFirstName() {
            // given
            VaultTestUtils.createKey(keyName, vaultContainer);
            CreateUserProfileRequest createUserProfileRequest =
                    CreateUserProfileRequest.builder()
                            .username(UUID.randomUUID().toString())
                            .firstName("Ivan")
                            .lastName("Ivanov")
                            .build();
            // when
            mockMvc.perform(
                    MockMvcRequestBuilders.post("/user-profiles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createUserProfileRequest))
                            .accept(MediaType.APPLICATION_JSON)
            );
            // then
            var userProfiles = userProfileService.findUserProfilesByFirstName(createUserProfileRequest.getFirstName());
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userProfiles).hasSize(1);
                softly.assertThat(userProfiles.getFirst().username()).isEqualTo(createUserProfileRequest.getUsername());
            });
        }

    }

}
