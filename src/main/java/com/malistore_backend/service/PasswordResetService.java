package com.malistore_backend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malistore_backend.data.entity.PasswordResetToken;
import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.PasswordResetTokenRepository;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.exception.InvalidCredentialsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Transactional
    public String generatePasswordResetToken(String email) {
        log.info("Generating password reset token for email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Vérifier s'il existe déjà un token pour cet utilisateur
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUserIdNative(user.getId());
        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token valide 1 heure
        
        PasswordResetToken resetToken;
        if (existingToken.isPresent()) {
            // Mettre à jour le token existant
            resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setExpiryDate(expiryDate);
            resetToken.setUsed(false);
            resetToken.setCreatedAt(LocalDateTime.now());
        } else {
            // Créer un nouveau token
            resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();
        }
        
        passwordResetTokenRepository.save(resetToken);
        
        // Envoyer l'email de réinitialisation
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
            log.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // On continue même si l'email échoue, le token est valide
        }
        
        log.info("Password reset token generated successfully for user: {}", user.getEmail());
        return token;
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token: {}", token.substring(0, 8) + "...");
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));
        
        // Vérifier si le token a expiré
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidCredentialsException("Reset token has expired");
        }
        
        // Mettre à jour le mot de passe
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marquer le token comme utilisé
        passwordResetTokenRepository.markTokenAsUsed(token);
        
        // Envoyer l'email de confirmation
        try {
            emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());
            log.info("Password reset confirmation email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", user.getEmail(), e);
            // On continue même si l'email échoue, la réinitialisation est réussie
        }
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired password reset tokens");
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
