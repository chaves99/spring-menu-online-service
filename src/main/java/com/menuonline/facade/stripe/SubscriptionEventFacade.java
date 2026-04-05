package com.menuonline.facade.stripe;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.stripe.StripeSubscriptionStatus;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.service.EmailService;
import com.menuonline.service.StripeService;
import com.menuonline.service.SubscriptionService;
import com.menuonline.service.UserService;
import com.menuonline.utils.SubscriptionConverter;
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
        subscriptionService.canceled(event);
        stripeService.findEmailByCustomer(event.customer()).ifPresentOrElse(email -> {
            // send email
        }, () -> log.warn("cancelSubscription - email not found for event:{}", event));
    }

    public void syncSubscription(String subscriptionId) {
        log.info("syncSubscription - subscriptionId:{}", subscriptionId);
        Optional<Subscription> subscription = stripeService.findSubscriptionById(subscriptionId);
        log.info("syncSubscription - subscription:{}", subscription);

        subscription.ifPresent(subs -> {
            StripeSubscriptionStatus status = StripeSubscriptionStatus.fromCode(subs.getStatus());
            String email = subs.getCustomerObject().getEmail();
            Optional<UserEntity> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("syncSubscription - user not found for subscription - email:{} subs:{}", email, subs);
                return;
            }

            subscriptionService.update(subscriptionId, userOpt.get(), SubscriptionConverter.convertStatus(status));

        });
    }
}
