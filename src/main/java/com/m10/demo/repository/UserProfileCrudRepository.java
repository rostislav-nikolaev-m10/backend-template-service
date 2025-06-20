package com.m10.demo.repository;

import com.m10.demo.entity.UserProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserProfileCrudRepository extends CrudRepository<UserProfileEntity, String> {

    List<UserProfileEntity> findByFirstNameHash(String firstNameHash);

}
