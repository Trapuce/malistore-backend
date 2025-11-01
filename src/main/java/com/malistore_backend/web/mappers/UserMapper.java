package com.malistore_backend.web.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.malistore_backend.data.entity.User;
import com.malistore_backend.web.dto.user.UserRegisterDto;
import com.malistore_backend.web.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", qualifiedByName = "encodePassword")
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRegisterDto userRegisterDto , @Context PasswordEncoder passwordEncoder);
    UserResponse toDto(User user);

    @Named("encodePassword")
    default String encodePassword(String rawPassword, @Context PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(rawPassword);
    }
}
