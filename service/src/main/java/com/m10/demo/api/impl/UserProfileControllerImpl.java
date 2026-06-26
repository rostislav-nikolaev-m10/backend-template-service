package com.m10.demo.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.m10.demo.api.doc.UserProfileController;
import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.api.doc.dto.GetUserProfileResponse;
import com.m10.demo.service.UserProfileService;


@RestController
@RequiredArgsConstructor
public class UserProfileControllerImpl implements UserProfileController {

    private final UserProfileService userProfileService;

    @Override
    public GetUserProfileResponse createUserProfile(@RequestBody CreateUserProfileRequest request) {
        return userProfileService.createUserProfile(request);
    }

    @Override
    public GetUserProfileResponse getUserProfileById(String userProfileId) {
        return userProfileService.getUserProfileById(userProfileId).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Профиль пользователя с ID " + userProfileId + " не найден"
            )
        );
    }

}
