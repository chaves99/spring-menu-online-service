package com.menuonline.payloads.stripe;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StripeSubscriptionStatus {

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
    public static StripeSubscriptionStatus fromCode(String status) {
        return Stream.of(values())
                .filter(e -> e.getValue().equals(status))
                .findFirst().orElse(null);
    }
}
