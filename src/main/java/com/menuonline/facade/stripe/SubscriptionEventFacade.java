package com.menuonline.facade.stripe;

import org.springframework.stereotype.Component;

import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.service.EmailService;
import com.menuonline.service.StripeService;
import com.menuonline.service.SubscriptionService;
import com.menuonline.service.UserService;

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
        subscriptionService.canceled(event);
        stripeService.findEmailByCustomer(event.customer()).ifPresentOrElse(email -> {
            // send email
        }, () -> log.warn("cancelSubscription - email not found for event:{}", event));
    }
}
