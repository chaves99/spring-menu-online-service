package com.menuonline.payloads;

import java.time.LocalDateTime;

import com.menuonline.entity.Subscription;
import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;

import lombok.Builder;

@Builder
public record LoginUserResponse(
        String token,
        String email,
        String establishmentName,
        String establishmentUrl,
        String establishmentDescription,
        String image,
        String subscription,
        String subscriptionStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static LoginUserResponse from(TokenAccess token) {
        UserEntity user = token.getUser();
        Subscription subs = Subscription
                .findCurrent(user.getSubscriptions());
        return new LoginUserResponse(token.getToken(),
                user.getEmail(),
                user.getEstablishmentName(),
                user.getEstablishmentUrl(),
                user.getEstablishmentDescription(),
                user.getImage(),
                subs.getDescription(),
                subs.getStatus().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
    
    public static LoginUserResponse from(UserEntity user, String token) {
        Subscription subs = Subscription
                .findCurrent(user.getSubscriptions());
        return new LoginUserResponse(token,
                user.getEmail(),
                user.getEstablishmentName(),
                user.getEstablishmentUrl(),
                user.getEstablishmentDescription(),
                user.getImage(),
                subs.getDescription(),
                subs.getStatus().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static LoginUserResponse from(UserEntity user, String token, String imageUrl) {
        Subscription subs = Subscription
                .findCurrent(user.getSubscriptions());
        return new LoginUserResponse(token,
                user.getEmail(),
                user.getEstablishmentName(),
                user.getEstablishmentUrl(),
                user.getEstablishmentDescription(),
                imageUrl,
                subs.getDescription(),
                subs.getStatus().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

}
