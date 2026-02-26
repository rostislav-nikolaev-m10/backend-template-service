package com.m10.demo.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfileModel {
    private UUID id;
    private final String username;
    private String firstName;
    private String lastName;
}
