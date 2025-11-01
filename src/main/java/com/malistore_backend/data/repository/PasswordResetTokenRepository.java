package com.malistore_backend.data.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.PasswordResetToken;
import com.malistore_backend.data.entity.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    
    Optional<PasswordResetToken> findByUser(User user);
    
    Optional<PasswordResetToken> findByUserId(Long userId);
    
    @Query(value = "SELECT * FROM password_reset_tokens WHERE user_id = :userId", nativeQuery = true)
    Optional<PasswordResetToken> findByUserIdNative(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.token = :token")
    void markTokenAsUsed(@Param("token") String token);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM password_reset_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteByUser(@Param("userId") Long userId);
}
