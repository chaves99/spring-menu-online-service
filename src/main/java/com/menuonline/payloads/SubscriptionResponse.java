package com.menuonline.payloads;

import java.time.LocalDateTime;
import java.util.List;

import com.menuonline.entity.Subscription;

public record SubscriptionResponse(SubscriptionResponseItem active,
        List<SubscriptionResponseItem> history) {

    public static SubscriptionResponseItem toSubscriptionItem(Subscription subs) {
        return new SubscriptionResponseItem(subs.getId(),
                subs.getFreeTier(),
                subs.getDescription(),
                subs.getStatus(),
                subs.getEndReason(),
                subs.getEndAt(),
                subs.getCreatedAt());
    }

    public static record SubscriptionResponseItem(String id,
            boolean freeTier,
            String description,
            Subscription.Status status,
            Subscription.EndReason endReason,
            LocalDateTime endDate,
            LocalDateTime createdAt) {
    }
}
