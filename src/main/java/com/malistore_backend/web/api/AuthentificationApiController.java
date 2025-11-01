package com.malistore_backend.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malistore_backend.service.AuthentificationService;
import com.malistore_backend.service.PasswordResetService;
import com.malistore_backend.web.dto.user.AuthResponse;
import com.malistore_backend.web.dto.user.ForgotPasswordDto;
import com.malistore_backend.web.dto.user.ResetPasswordDto;
import com.malistore_backend.web.dto.user.UserLoginDto;
import com.malistore_backend.web.dto.user.UserRegisterDto;
import com.malistore_backend.web.dto.user.UserResponse;
import com.malistore_backend.web.payload.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthentificationApiController {
    
    private final AuthentificationService authentificationService;
    private final PasswordResetService passwordResetService;

    public AuthentificationApiController(AuthentificationService authentificationService, 
                                       PasswordResetService passwordResetService) {
        this.authentificationService = authentificationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        UserResponse userResponse = authentificationService.registerUser(userRegisterDto);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody UserLoginDto userLoginDto) {
        AuthResponse authResponse = authentificationService.loginUser(userLoginDto);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        String token = passwordResetService.generatePasswordResetToken(forgotPasswordDto.getEmail());
        // En mode test, on retourne aussi le token pour faciliter les tests
        return ResponseEntity.ok(ApiResponse.success("Un email de réinitialisation a été envoyé à votre adresse email. Token de test: " + token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        passwordResetService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Votre mot de passe a été réinitialisé avec succès. Un email de confirmation a été envoyé."));
    }
}