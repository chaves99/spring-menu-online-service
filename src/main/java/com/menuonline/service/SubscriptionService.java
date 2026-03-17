package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.stripe.StripeWebhookInvoice;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionCancelled;
import com.menuonline.repository.SubscriptionRepository;
import com.menuonline.repository.UserRepository;
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
        subscription.setStatus(Subscription.Status.CANCELLED);
        subscription.setEndAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    public void verifyUserFreeTier(UserEntity user) {
        Optional<Subscription> first = user.getSubscriptions().stream().filter(s -> s.getFreeTier()).findFirst();
        first.ifPresent(subs -> {
            if (subs.getEndAt().isBefore(LocalDateTime.now())) {
                subs.setStatus(Subscription.Status.CANCELLED);
                subscriptionRepository.save(subs);
            }
        });
    }

    public void updateSubs(StripeWebhookInvoice invoice) {
        log.info("updatesubs - invoice:{}", invoice);
        Optional<UserEntity> userOpt = userRepository.findByEmail(invoice.customerEmail());

        if (userOpt.isEmpty()) {
            log.error("updateSubs - email not found - invoice:{}", invoice);
            return;
        }

        var user = userOpt.get();
        Optional<Subscription> subscriptionOpt = user.getSubscriptions().stream()
                .filter(s -> s.getId().equals(invoice.id())).findFirst();

        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            subscription.setStatus(Subscription.Status.ACTIVE);
            subscription.setEndAt(invoice.endDate());
            subscriptionRepository.save(subscription);
        } else {
            Subscription newSubs = new Subscription();
            newSubs.setId(invoice.id());
            newSubs.setCustomerId(invoice.customer());
            newSubs.setDescription("Plano Unico.");
            newSubs.setUser(user);
            newSubs.setEndAt(invoice.endDate());
            newSubs.setFreeTier(false);
            newSubs.setStatus(Subscription.Status.ACTIVE);
            subscriptionRepository.save(newSubs);
        }

    }

    public void paymentFail(StripeWebhookInvoice invoice) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(invoice.customerEmail());

        if (userOpt.isEmpty()) {
            log.error("paymentFail - email not found - invoice:{}", invoice);
            return;
        }

        var user = userOpt.get();
        Optional<Subscription> subscription = user.getSubscriptions().stream()
                .filter(s -> s.getId().equals(invoice.id())).findFirst();

        subscription.ifPresentOrElse(subs -> {
            subs.setStatus(Subscription.Status.PAYMENT_FAILED);
        }, () -> {
            log.error("paymentFail - subscription not found - invoice:{}", invoice);
            return;
        });

    }

    public void cancelled(StripeWebhookSubscriptionCancelled subsCancelled) {
        subscriptionRepository
                .findByIdAndCustomerId(subsCancelled.id(), subsCancelled.customer())
                .ifPresentOrElse(
                        subs -> {
                            subs.setStatus(Subscription.Status.CANCELLED);
                            subs.setEndAt(subsCancelled.endedAt());
                            subscriptionRepository.save(subs);
                        },
                        () -> log.warn("subscription cancelled not found: {}", subsCancelled));
    }

}
