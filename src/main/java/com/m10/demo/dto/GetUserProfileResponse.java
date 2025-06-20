package com.m10.demo.dto;

import lombok.Builder;

import java.util.UUID;


@Builder
public record GetUserProfileResponse(
        UUID id,
        String username,
        String firstName,
        String lastName
) {
}
