package com.m10.demo.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import com.m10.demo.entity.UserProfileEntity;

public interface UserProfileCrudRepository extends CrudRepository<UserProfileEntity, String> {

    List<UserProfileEntity> findByFirstNameHash(String firstNameHash);

}
