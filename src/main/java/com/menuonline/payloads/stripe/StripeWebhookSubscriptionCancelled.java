package com.menuonline.payloads.stripe;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.menuonline.utils.DateUtils;

public record StripeWebhookSubscriptionCancelled(String id, String customer, LocalDateTime endedAt) {

    public StripeWebhookSubscriptionCancelled(String id, String customer, @JsonProperty("ended_at") Long endedAt) {
        this(id, customer, DateUtils.secondsToObject(endedAt));
    }

}
