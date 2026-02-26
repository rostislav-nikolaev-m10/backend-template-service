package com.m10.demo.api.doc.dto;

import lombok.Builder;


@Builder
public record CreateUserProfileRequest(
    String username,
    String firstName,
    String lastName
) {
}
