package com.menuonline.payloads.stripe;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.menuonline.utils.DateUtils;

public record StripeWebhookSubscriptionEvent(String id,
        String customer,
        String description,
        @JsonProperty("start_date") Long startDate,
        @JsonProperty("ended_at") Long endedAt,
        StripeSubscriptionStatus status,
        SubscriptionItems items) {

    public static record SubscriptionItems(List<SubscriptionItem> data) {
    }

    public static record SubscriptionItem(LocalDateTime currentPeriodEnd,
            LocalDateTime currentPeriodStart) {
        public SubscriptionItem(@JsonProperty("current_period_end") Long currentPeriodEnd,
                @JsonProperty("current_period_start") Long currentPeriodStart) {
            this(DateUtils.secondsToObject(currentPeriodEnd),
                    DateUtils.secondsToObject(currentPeriodStart));
        }
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return items().data().get(0).currentPeriodEnd();
    }

}
