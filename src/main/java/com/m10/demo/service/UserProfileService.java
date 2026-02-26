package com.m10.demo.service;


import java.util.List;
import java.util.Optional;

import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.api.doc.dto.GetUserProfileResponse;

public interface UserProfileService {

    GetUserProfileResponse createUserProfile(CreateUserProfileRequest request);

    Optional<GetUserProfileResponse> getUserProfileById(String userProfileId);

    List<GetUserProfileResponse> findUserProfilesByFirstName(String firstName);

}
