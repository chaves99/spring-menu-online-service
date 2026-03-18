package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.stripe.StripeWebhookInvoice;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.repository.SubscriptionRepository;
import com.menuonline.repository.UserRepository;
import com.menuonline.utils.SubscriptionStatusConverter;
import com.menuonline.utils.TokenGeneratorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    @Value("${freetier.days}")
    private Integer freeTierDays;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public Subscription createFreeTier(UserEntity user) {
        Subscription subs = new Subscription();
        user.getSubscriptions().add(subs);
        subs.setUser(user);
        subs.setDescription("FREE TIER");
        subs.setFreeTier(true);
        subs.setEndAt(LocalDateTime.now().plusDays(freeTierDays));
        subs.setStatus(Subscription.Status.ACTIVE);
        subs.setId("u" + user.getId() + "_" + TokenGeneratorUtil.generate(20));
        return subscriptionRepository.save(subs);
    }

    public Subscription cancel(UserEntity user, Subscription subscription) {
        subscription.setStatus(Subscription.Status.CANCELED);
        subscription.setEndAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    public void verifyUserFreeTier(UserEntity user) {
        Optional<Subscription> first = user.getSubscriptions().stream().filter(s -> s.getFreeTier()).findFirst();
        first.ifPresent(subs -> {
            if (subs.getEndAt().isBefore(LocalDateTime.now())) {
                subs.setStatus(Subscription.Status.CANCELED);
                subscriptionRepository.save(subs);
            }
        });
    }

    public void createSubscription(StripeWebhookSubscriptionEvent event, UserEntity user) {
        Subscription newSubs = new Subscription();
        newSubs.setId(event.id());
        newSubs.setCustomerId(event.customer());
        newSubs.setDescription("Plano Unico.");
        newSubs.setUser(user);
        newSubs.setEndAt(event.getCurrentPeriodEnd());
        newSubs.setFreeTier(false);
        newSubs.setStatus(SubscriptionStatusConverter.convert(event.status()));
        log.info("createSubscription - new subscription:{}", newSubs);
        subscriptionRepository.save(newSubs);
        this.finishFreeTier(user);
    }

    public void paymentFail(StripeWebhookInvoice invoice) {
        log.info("paymentFail - invoice:{}", invoice);
        Optional<UserEntity> userOpt = userRepository.findByEmail(invoice.customerEmail());

        if (userOpt.isEmpty()) {
            log.error("paymentFail - email not found - invoice:{}", invoice);
            return;
        }

        var user = userOpt.get();
        Optional<Subscription> subscription = user.getSubscriptions().stream()
                .filter(s -> s.getId().equals(invoice.id())).findFirst();

        subscription.ifPresentOrElse(subs -> {
            subs.setStatus(Subscription.Status.UNPAID);
        }, () -> {
            log.error("paymentFail - subscription not found - invoice:{}", invoice);
            return;
        });

    }

    public void canceled(StripeWebhookSubscriptionEvent event) {
        log.info("canceled - event:{}", event);
        subscriptionRepository
                .findByIdAndCustomerId(event.id(), event.customer())
                .ifPresentOrElse(subs -> {
                    subs.setStatus(Subscription.Status.CANCELED);
                    subs.setEndAt(event.getCurrentPeriodEnd());
                    subscriptionRepository.save(subs);
                }, () -> log.warn("subscription canceled not found: {}", event));
    }

    public void finishFreeTier(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        for (var subs : user.getSubscriptions()) {
            if (subs.getFreeTier() && subs.getEndAt().isBefore(now)) {
                subs.setEndAt(now);
                subs.setStatus(Subscription.Status.CANCELED);
                subscriptionRepository.save(subs);
            }
        }
    }

}
