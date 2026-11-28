package com.you.springsecurity.user.repository;

import com.you.springsecurity.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.username = :username
            """)
    Optional<Users> findByUsernameWithRoleAndPermissions(
            @Param("username") String username
    );
    @Query("""
        SELECT u
        FROM Users u
        JOIN FETCH u.role r
        WHERE u.id = :userId
    """)
    Optional<Users> findWithRoleById(
            @Param("userId") Long userId
    );
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
        SELECT DISTINCT u
        FROM Users u
        JOIN FETCH u.role r
        LEFT JOIN FETCH r.permissions
        WHERE u.username = :username
    """)
    Optional<Users> findByUsernameWithRoles(
            @Param("username") String username
    );
}