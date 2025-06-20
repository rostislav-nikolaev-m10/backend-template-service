package com.m10.demo.service.impl;

import java.util.Optional;

import com.m10.demo.dto.GetUserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.m10.demo.dto.CreateUserProfileRequest;
import com.m10.demo.entity.UserProfileEntity;
import com.m10.demo.mapper.UserProfileMapper;
import com.m10.demo.model.UserProfileModel;
import com.m10.demo.repository.UserProfileCrudRepository;
import com.m10.demo.service.UserProfileService;


@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileCrudRepository userProfileCrudRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public GetUserProfileResponse createUserProfile(CreateUserProfileRequest request) {
        UserProfileModel userProfileModel = userProfileMapper.createRequestToModel(request);
        UserProfileEntity userProfileEntity = userProfileMapper.modelToEntity(userProfileModel);
        UserProfileEntity createdUserProfileEntity = userProfileCrudRepository.save(userProfileEntity);
        UserProfileModel createdUserProfileModel = userProfileMapper.entityToModel(createdUserProfileEntity);
        GetUserProfileResponse getUserProfileResponse = userProfileMapper.modelToGetResponse(createdUserProfileModel);
        return getUserProfileResponse;
    }

    @Override
    public Optional<GetUserProfileResponse> getUserProfileById(String userProfileId) {
        Optional<UserProfileEntity> userProfileEntityOpt = userProfileCrudRepository.findById(userProfileId);
        return userProfileEntityOpt
                .map(userProfileMapper::entityToModel)
                .map(userProfileMapper::modelToGetResponse);
    }

}
