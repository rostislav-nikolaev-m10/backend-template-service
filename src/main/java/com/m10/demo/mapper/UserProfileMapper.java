package com.m10.demo.mapper;

import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.api.doc.dto.GetUserProfileResponse;
import com.m10.demo.entity.UserProfileEntity;
import com.m10.demo.model.UserProfileModel;

public interface UserProfileMapper {

    UserProfileModel createRequestToModel(CreateUserProfileRequest request);

    UserProfileEntity modelToEntity(UserProfileModel model);

    UserProfileModel entityToModel(UserProfileEntity entity);

    GetUserProfileResponse modelToGetResponse(UserProfileModel model);

}
