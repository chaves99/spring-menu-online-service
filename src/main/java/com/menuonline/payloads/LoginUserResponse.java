package com.menuonline.payloads;

import java.time.LocalDateTime;

import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;

public record LoginUserResponse(
        String token,
        String email,
        String establishmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static LoginUserResponse from(TokenAccess token) {
        UserEntity user = token.getUser();
        return new LoginUserResponse(token.getToken(),
                user.getEmail(),
                user.getEstablishmentName(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
