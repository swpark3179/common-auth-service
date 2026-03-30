package com.idp.repository;

import com.idp.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByResourcePatternAndAction(String resourcePattern, String action);

    boolean existsByResourcePatternAndAction(String resourcePattern, String action);
}
