package com.m10.demo.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class UserProfileModel {
    private UUID id;
    private final String username;
    private String firstName;
    private String lastName;
}
