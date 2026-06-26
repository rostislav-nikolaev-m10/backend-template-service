package com.m10.integration.test;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.service.UserProfileService;
import com.m10.testkit.vault.VaultContainerManager;
import com.m10.integration.test.support.CommonIT;


public class CreateUserIT extends CommonIT {

    private static final String TEST_KEY = "test-key";

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private VaultContainerManager vaultContainerManager;

    @Nested
    class Success {

        @Test
        void test() {
            vaultContainerManager.createKey(TEST_KEY);
            CreateUserProfileRequest createUserProfileRequest =
                CreateUserProfileRequest.builder()
                    .username(UUID.randomUUID().toString())
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .build();
            var response = userProfileService.createUserProfile(createUserProfileRequest);
            vaultContainerManager.rotateKey(TEST_KEY);
            var secondResponse = userProfileService.getUserProfileById(response.id().toString()).get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response).isNotNull();
                softly.assertThat(secondResponse).isNotNull();
            });
        }

        @Test
        @DisplayName("Успешное создание профиля пользователя")
        void saveNewUser() {
            // given
            vaultContainerManager.createKey(TEST_KEY);
            CreateUserProfileRequest createUserProfileRequest =
                CreateUserProfileRequest.builder()
                    .username(UUID.randomUUID().toString())
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .build();
            // when & then
            webTestClient.post().uri("/user-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserProfileRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.username").isEqualTo(createUserProfileRequest.username())
                .jsonPath("$.firstName").isEqualTo(createUserProfileRequest.firstName())
                .jsonPath("$.lastName").isEqualTo(createUserProfileRequest.lastName());
        }

        @Test
        @DisplayName("Можем прочитать профиль пользователя после создания")
        void getNewUserAfterCreate() {
            // given
            vaultContainerManager.createKey(TEST_KEY);
            CreateUserProfileRequest createUserProfileRequest =
                CreateUserProfileRequest.builder()
                    .username(UUID.randomUUID().toString())
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .build();
            // when
            String responseBody = webTestClient.post().uri("/user-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserProfileRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
            String userId = JsonPath.read(responseBody, "$.id");
            // then
            webTestClient.get().uri("/user-profiles/{id}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId)
                .jsonPath("$.username").isEqualTo(createUserProfileRequest.username())
                .jsonPath("$.firstName").isEqualTo(createUserProfileRequest.firstName())
                .jsonPath("$.lastName").isEqualTo(createUserProfileRequest.lastName());
        }

        @Test
        @DisplayName("Можем прочитать профиль пользователя после создания и ротации ключа")
        void getNewUserAfterCreateAndKeyRotation() {
            // given
            vaultContainerManager.createKey(TEST_KEY);
            CreateUserProfileRequest createUserProfileRequest =
                CreateUserProfileRequest.builder()
                    .username(UUID.randomUUID().toString())
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .build();
            // when
            String responseBody = webTestClient.post().uri("/user-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserProfileRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
            vaultContainerManager.rotateKey(TEST_KEY);
            String userId = JsonPath.read(responseBody, "$.id");
            // then
            webTestClient.get().uri("/user-profiles/{id}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId)
                .jsonPath("$.username").isEqualTo(createUserProfileRequest.username())
                .jsonPath("$.firstName").isEqualTo(createUserProfileRequest.firstName())
                .jsonPath("$.lastName").isEqualTo(createUserProfileRequest.lastName());
        }

        @Test
        @DisplayName("Можем найти профиль пользователя по имени")
        void findUserByFirstName() {
            // given
            vaultContainerManager.createKey(TEST_KEY);
            CreateUserProfileRequest createUserProfileRequest =
                CreateUserProfileRequest.builder()
                    .username(UUID.randomUUID().toString())
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .build();
            // when
            webTestClient.post().uri("/user-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserProfileRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
            // then
            var userProfiles = userProfileService.findUserProfilesByFirstName(createUserProfileRequest.firstName());
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userProfiles).hasSize(1);
                softly.assertThat(userProfiles.getFirst().username()).isEqualTo(createUserProfileRequest.username());
            });
        }

    }

}
