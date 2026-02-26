package com.m10.demo.api.doc.dto;

import java.util.UUID;
import lombok.Builder;


@Builder
public record GetUserProfileResponse(
    UUID id,
    String username,
    String firstName,
    String lastName
) {
}
