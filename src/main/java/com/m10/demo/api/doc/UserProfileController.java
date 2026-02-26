package com.m10.demo.api.doc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.m10.demo.api.doc.dto.CreateUserProfileRequest;
import com.m10.demo.api.doc.dto.GetUserProfileResponse;


@RequestMapping("/user-profiles")
public interface UserProfileController {

    @PostMapping
    GetUserProfileResponse createUserProfile(@RequestBody CreateUserProfileRequest request);

    @GetMapping("/{userProfileId}")
    GetUserProfileResponse getUserProfileById(@PathVariable String userProfileId);

}
