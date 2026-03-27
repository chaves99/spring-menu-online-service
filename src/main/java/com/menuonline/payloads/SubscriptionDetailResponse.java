package com.menuonline.payloads;

import java.time.LocalDateTime;

import com.menuonline.utils.DateUtils;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethod.Card;
import com.stripe.model.Subscription;

public record SubscriptionDetailResponse(String id,
        LocalDateTime billingCycleAncher,
        LocalDateTime startDate,
        String cardBrand,
        String cardExpirationMonth,
        String cardExpirationYear,
        LocalDateTime cardCreated,
        String cardLastDigits){

    public static SubscriptionDetailResponse from( Subscription subs) {
        PaymentMethod paymentMethod = subs.getDefaultPaymentMethodObject();
        Card card = paymentMethod.getCard();
        return new SubscriptionDetailResponse(subs.getId(),
                DateUtils.secondsToObject(subs.getBillingCycleAnchor()),
                DateUtils.secondsToObject(subs.getStartDate()),
                card.getBrand(),
                card.getExpMonth().toString(),
                card.getExpYear().toString(),
                DateUtils.secondsToObject(paymentMethod.getCreated()),
                card.getLast4());
    }
}
