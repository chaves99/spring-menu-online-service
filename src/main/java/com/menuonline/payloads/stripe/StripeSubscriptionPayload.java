package com.menuonline.payloads.stripe;

public record StripeSubscriptionPayload(String id,
        String latestInvoice
        ) {

    public static record StripeSubscriptionItemsPayload(){}
}
