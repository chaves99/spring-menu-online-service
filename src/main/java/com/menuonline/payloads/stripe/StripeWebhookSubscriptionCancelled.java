package com.menuonline.payloads.stripe;

import java.time.LocalDateTime;

import com.menuonline.utils.DateUtils;

public record StripeWebhookSubscriptionCancelled(String id, String customer, LocalDateTime endedAt) {

    public StripeWebhookSubscriptionCancelled(String id, String customer, Long endedAt) {
        this(id, customer, DateUtils.secondsToObject(endedAt));
    }

}
