package com.menuonline.facade.stripe;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.menuonline.entity.Subscription.EndReason;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.stripe.StripeSubscriptionStatus;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.service.EmailService;
import com.menuonline.service.StripeService;
import com.menuonline.service.SubscriptionService;
import com.menuonline.service.UserService;
import com.stripe.model.Subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventFacade {

    private final SubscriptionService subscriptionService;
    private final StripeService stripeService;
    private final EmailService emailService;
    private final UserService userService;

    public void newSubscription(StripeWebhookSubscriptionEvent event) {
        log.info("newSubscription - event:{}", event);
        stripeService.findEmailByCustomer(event.customer()).ifPresentOrElse(email -> {
            userService.findByEmail(email).ifPresentOrElse(user -> {
                subscriptionService.createSubscription(event, user);
                subscriptionService.finishFreeTier(user);
            }, () -> log.error("newSubscription - email not found - email:{} event:{}", email, event));
        }, () -> log.warn("newSubscription - email not found for event:{}", event));
    }

    public void cancelSubscription(StripeWebhookSubscriptionEvent event) {
        log.info("cancelSubscription - event:{}", event);
        subscriptionService.canceled(event).ifPresent(subs -> {
            UserEntity user = subs.getUser();
            log.info("cancelSubscription - subscription:{} user:{}", subs, user);
            if (subs.getEndReason().equals(EndReason.UNPAID)) {
                emailService.sendPastDuePayment(user.getEmail());
            } else {
                emailService.subscriptionCancel(user.getEmail());
            }
        });
    }

    public void syncSubscription(StripeWebhookSubscriptionEvent event) {
        String subscriptionId = event.id();
        Optional<Subscription> subscription = stripeService.findSubscriptionById(subscriptionId);
        log.info("syncSubscription - id:{} subscription:{}", subscriptionId, subscription);

        if (subscription.isPresent()) {
            Subscription subs = subscription.get();

            StripeSubscriptionStatus status = StripeSubscriptionStatus.fromCode(subs.getStatus());

            if (status.equals(StripeSubscriptionStatus.PAST_DUE)) {
                String email = subs.getCustomerObject().getEmail();
                Optional<UserEntity> userOpt = userService.findByEmail(email);
                if (userOpt.isEmpty()) {
                    log.warn("syncSubscription - user not found for subscription - email:{} subs:{}", email, subs);
                    return;
                }
                subscriptionService.setEndReasonPastDue(subscriptionId);
                stripeService.cancel(subscriptionId);
            } else if (status.equals(StripeSubscriptionStatus.ACTIVE)) {
                subscriptionService.updateEndAt(subscriptionId, event.getCurrentPeriodEnd());
            } else {
                log.warn("syncSubscription - status unhandled:{}", status);
            }

        }
    }
}
