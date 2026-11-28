package com.you.springsecurity.user.repository;

import com.you.springsecurity.user.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository
        extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
    Optional<Permission> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}