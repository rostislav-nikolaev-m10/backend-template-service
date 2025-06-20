package com.m10.demo.repository;

import com.m10.demo.entity.UserProfileEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserProfileCrudRepository extends CrudRepository<UserProfileEntity, String> {
}
