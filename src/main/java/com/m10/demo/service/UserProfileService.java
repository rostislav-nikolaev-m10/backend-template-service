package com.m10.demo.service;

import com.m10.demo.dto.CreateUserProfileRequest;
import com.m10.demo.dto.GetUserProfileResponse;

import java.util.List;
import java.util.Optional;

public interface UserProfileService {

    GetUserProfileResponse createUserProfile(CreateUserProfileRequest request);

    Optional<GetUserProfileResponse> getUserProfileById(String userProfileId);

    List<GetUserProfileResponse> findUserProfilesByFirstName(String firstName);

}
