package com.menuonline.utils;

import com.menuonline.entity.Subscription;
import com.menuonline.payloads.stripe.StripeSubscriptionStatus;

public final record SubscriptionConverter() {

    public static final Subscription.Status convertStatus(StripeSubscriptionStatus stripeStatus) {
        if (stripeStatus.equals(StripeSubscriptionStatus.ACTIVE))
            return Subscription.Status.ACTIVE;

        if (stripeStatus.equals(StripeSubscriptionStatus.UNPAID))
            return Subscription.Status.UNPAID;

        return Subscription.Status.CANCELED;

    }

    public static Subscription createFrom(com.stripe.model.Subscription stripeSubs) {
        Subscription subscription = new Subscription();
        subscription.setId(stripeSubs.getId());
        subscription.setDescription(stripeSubs.getDescription());
        return subscription;
    }
}
