package com.menuonline.utils;

import com.menuonline.entity.Subscription;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;

public final record SubscriptionStatusConverter() {

    public static final Subscription.Status convert(StripeWebhookSubscriptionEvent.Status stripeStatus) {
        if (stripeStatus.equals(StripeWebhookSubscriptionEvent.Status.ACTIVE))
            return Subscription.Status.ACTIVE;

        if (stripeStatus.equals(StripeWebhookSubscriptionEvent.Status.UNPAID))
            return Subscription.Status.UNPAID;

        return Subscription.Status.CANCELED;

    }
}
