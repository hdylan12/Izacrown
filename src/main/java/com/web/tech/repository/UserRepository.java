package com.web.tech.repository;

import com.web.tech.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    User findByPhone(String phone);

    @Query("{$or: [{firstName: {$regex: ?0, $options: 'i'}}, {lastName: {$regex: ?0, $options: 'i'}}, {email: {$regex: ?0, $options: 'i'}}]}")
    Page<User> findByNameOrEmail(String query, Pageable pageable);

    @Query("{role: {$ne: ?0}}")
    Page<User> findByRolesNotContaining(String role, Pageable pageable);

    @Query("{$and: [{$or: [{firstName: {$regex: ?0, $options: 'i'}}, {lastName: {$regex: ?0, $options: 'i'}}, {email: {$regex: ?0, $options: 'i'}}]}, {role: {$ne: ?1}}]}")
    Page<User> findByNameOrEmailAndRolesNotContaining(String query, String role, Pageable pageable);

    @Query(value = "{role: {$ne: ?0}}", count = true)
    Long countByRoleNot(String role);
}