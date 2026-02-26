package com.m10.demo.mapper.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.VaultDecryptionResult;
import org.springframework.vault.support.VaultEncryptionResult;

import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.api.doc.dto.GetUserProfileResponse;
import com.m10.demo.entity.UserProfileEntity;
import com.m10.demo.mapper.UserProfileMapper;
import com.m10.demo.model.UserProfileModel;
import com.m10.demo.service.HashingService;


@Component
@RequiredArgsConstructor
public class UserProfileMapperImpl implements UserProfileMapper {

    @Value("${vault.encryption.key-name}")
    private String keyName;

    private final VaultTemplate vault;
    private final HashingService hashingService;

    @Override
    public UserProfileModel createRequestToModel(CreateUserProfileRequest request) {
        return UserProfileModel.builder()
            .username(request.username())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .build();
    }

    @Override
    public UserProfileEntity modelToEntity(UserProfileModel model) {
        List<VaultEncryptionResult> encryptionResults =
            vault.opsForTransit().encrypt(
                keyName,
                List.of(
                    Plaintext.of(model.getFirstName()),
                    Plaintext.of(model.getLastName())
                )
            );

        return UserProfileEntity.builder()
            .id(model.getId())
            .username(model.getUsername())
            .firstName(encryptionResults.get(0).get().getCiphertext())
            .firstNameHash(hashingService.hash(model.getFirstName()))
            .lastName(encryptionResults.get(1).get().getCiphertext())
            .lastNameHash(hashingService.hash(model.getLastName()))
            .build();
    }

    @Override
    public UserProfileModel entityToModel(UserProfileEntity entity) {
        List<VaultDecryptionResult> decryptionResult =
            vault.opsForTransit().decrypt(
                keyName,
                List.of(
                    Ciphertext.of(entity.getFirstName()),
                    Ciphertext.of(entity.getLastName())
                )
            );
        return UserProfileModel.builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .firstName(decryptionResult.get(0).get().asString())
            .lastName(decryptionResult.get(1).get().asString())
            .build();
    }

    @Override
    public GetUserProfileResponse modelToGetResponse(UserProfileModel model) {
        return GetUserProfileResponse.builder()
            .id(model.getId())
            .username(model.getUsername())
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .build();
    }

}
