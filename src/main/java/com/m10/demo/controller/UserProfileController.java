package com.m10.demo.controller;

import com.m10.demo.dto.CreateUserProfileRequest;
import com.m10.demo.dto.GetUserProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/user-profiles")
public interface UserProfileController {

    @PostMapping
    GetUserProfileResponse createUserProfile(@RequestBody CreateUserProfileRequest request);

    @GetMapping("/{userProfileId}")
    GetUserProfileResponse getUserProfileById(@PathVariable String userProfileId);

}
