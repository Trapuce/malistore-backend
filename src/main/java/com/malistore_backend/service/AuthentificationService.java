package com.malistore_backend.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.data.repository.UserRepository;
import com.malistore_backend.security.JwtUtil;
import com.malistore_backend.security.UserDetailsServiceImpl;
import com.malistore_backend.web.dto.user.AuthResponse;
import com.malistore_backend.web.dto.user.UserLoginDto;
import com.malistore_backend.web.dto.user.UserRegisterDto;
import com.malistore_backend.web.dto.user.UserResponse;
import com.malistore_backend.web.exception.DuplicateResourceException;
import com.malistore_backend.web.exception.InvalidCredentialsException;
import com.malistore_backend.web.exception.ResourceNotFoundException;
import com.malistore_backend.web.mappers.UserMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthentificationService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

  

    public UserResponse registerUser(UserRegisterDto userRegisterDto) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByEmail(userRegisterDto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User with email " + userRegisterDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userRegisterDto, passwordEncoder);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public AuthResponse loginUser(UserLoginDto userLoginDto) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(userLoginDto.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userLoginDto.getEmail()));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Vérifier si l'utilisateur est actif
        if (user.getStatus() != com.malistore_backend.data.entity.UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        // Générer les tokens
        String token = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(user.getEmail()));

        // Créer la réponse
        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .user(userMapper.toDto(user))
            .build();
    }

}
