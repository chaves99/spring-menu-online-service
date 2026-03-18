package com.menuonline.payloads.stripe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.menuonline.utils.DateUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record StripeWebhookSubscriptionEvent(String id,
        String customer,
        Long startDate,
        Status status,
        SubscriptionItems items) {

    @RequiredArgsConstructor
    @Getter
    public static enum Status {
        INCOMPLETE("incomplete"),
        INCOMPLETE_EXPIRED("incomplete_expired"),
        TRIALING("trialing"),
        ACTIVE("active"),
        PAST_DUE("past_due"),
        CANCELED("canceled"),
        UNPAID("unpaid"),
        PAUSED("paused");

        private final String value;

        @JsonCreator
        public static Status fromCode(String status) {
            return Stream.of(values())
                    .filter(e -> e.getValue().equals(status))
                    .findFirst().orElse(null);
        }
    }

    public static record SubscriptionItems(List<SubscriptionItem> data) {
    }

    public static record SubscriptionItem(LocalDateTime currentPeriodEnd, LocalDateTime currentPeriodStart) {

        public SubscriptionItem(Long currentPeriodEnd, Long currentPeriodStart) {
            this(DateUtils.secondsToObject(currentPeriodEnd),
                    DateUtils.secondsToObject(currentPeriodStart));
        }
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return items().data().get(0).currentPeriodEnd();
    }

}
