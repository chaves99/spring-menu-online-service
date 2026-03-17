package com.menuonline.payloads;

import java.time.LocalDateTime;

import com.menuonline.entity.Subscription;

public record SubscriptionResponse(String id, Boolean freeTier,
        String description,
        Subscription.Status status,
        LocalDateTime endDate,
        LocalDateTime createdAt){

    public static SubscriptionResponse from(Subscription subs) {
        return new SubscriptionResponse(subs.getId(),
                subs.getFreeTier(),
                subs.getDescription(),
                subs.getStatus(),
                subs.getEndAt(),
                subs.getCreatedAt());
    }
}
