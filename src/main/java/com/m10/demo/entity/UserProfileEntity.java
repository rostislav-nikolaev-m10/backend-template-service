package com.m10.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@Table(schema = "demo", value = "user_profile")
public class UserProfileEntity {
    @Id
    private UUID id;
    private String username;
    private String firstName;
    private String firstNameHash;
    private String lastName;
    private String lastNameHash;

    public UserProfileEntity() {
    }

}
