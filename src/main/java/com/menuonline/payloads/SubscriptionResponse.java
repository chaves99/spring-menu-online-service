package com.menuonline.payloads;

import java.time.LocalDateTime;

import com.menuonline.entity.Subscription;

public record SubscriptionResponse(String id, Boolean freeTier,
        Subscription.Status status,
        LocalDateTime endDate,
        LocalDateTime createdAt){

    public static SubscriptionResponse from(Subscription subs) {
        return new SubscriptionResponse(subs.getId(),
                subs.getFreeTier(),
                subs.getStatus(),
                subs.getEndAt(),
                subs.getCreatedAt());
    }
}
