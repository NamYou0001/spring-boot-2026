package com.you.springsecurity.auth.token;

import com.you.springsecurity.auth.token.RefreshToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    // Find token with user
    @Query("""
            SELECT rt
            FROM RefreshToken rt
            JOIN FETCH rt.user
            WHERE rt.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashWithUser(
            @Param("tokenHash") String tokenHash
    );

    // Find token with user and lock database row
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rt
            FROM RefreshToken rt
            JOIN FETCH rt.user
            WHERE rt.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashWithUserForUpdate(
            @Param("tokenHash") String tokenHash
    );

    List<RefreshToken> findByFamilyId(String familyId);

    Optional<RefreshToken> findBySessionId(String sessionId);

    // Revoke entire token family
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revokedAt = :revokedAt,
                rt.revokedReason = :reason
            WHERE rt.familyId = :familyId
              AND rt.revokedAt IS NULL
            """)
    int revokeFamily(
            @Param("familyId") String familyId,
            @Param("revokedAt") Instant revokedAt,
            @Param("reason") String reason
    );

    // Revoke all user tokens
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revokedAt = :revokedAt,
                rt.revokedReason = :reason
            WHERE rt.user.id = :userId
              AND rt.revokedAt IS NULL
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") Instant revokedAt,
            @Param("reason") String reason
    );

    // Revoke specific session
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revokedAt = :revokedAt,
                rt.revokedReason = :reason
            WHERE rt.sessionId = :sessionId
              AND rt.revokedAt IS NULL
            """)
    int revokeBySessionId(
            @Param("sessionId") String sessionId,
            @Param("revokedAt") Instant revokedAt,
            @Param("reason") String reason
    );

    // Get active sessions
    @Query("""
            SELECT rt
            FROM RefreshToken rt
            WHERE rt.user.id = :userId
              AND rt.revokedAt IS NULL
              AND rt.expiresAt > :now
            ORDER BY rt.createdAt DESC
            """)
    List<RefreshToken> findActiveSessionsByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}