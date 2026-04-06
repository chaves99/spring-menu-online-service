package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.SubscriptionResponse;
import com.menuonline.payloads.SubscriptionResponse.SubscriptionResponseItem;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.repository.SubscriptionRepository;
import com.menuonline.utils.DateUtils;
import com.menuonline.utils.SubscriptionConverter;
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
        subscription.setEndReason(Subscription.EndReason.USER_CANCEL);
        return subscriptionRepository.save(subscription);
    }

    public void verifyUserFreeTier(UserEntity user) {
        Optional<Subscription> first = user.getSubscriptions().stream().filter(s -> s.getFreeTier()).findFirst();
        first.ifPresent(subs -> {
            boolean before = subs.getEndAt().isBefore(LocalDateTime.now());
            if (before) {
                subs.setStatus(Subscription.Status.CANCELED);
                subscriptionRepository.save(subs);
            }
        });
    }

    public void createSubscription(StripeWebhookSubscriptionEvent event, UserEntity user) {
        Subscription newSubs = new Subscription();
        newSubs.setId(event.id());
        newSubs.setCustomerId(event.customer());
        newSubs.setDescription(event.description() != null ? event.description() : "Assinatura");
        newSubs.setUser(user);
        newSubs.setEndAt(event.getCurrentPeriodEnd());
        newSubs.setFreeTier(false);
        newSubs.setStatus(SubscriptionConverter.convertStatus(event.status()));
        log.info("createSubscription - new subscription:{}", newSubs);
        subscriptionRepository.save(newSubs);
    }

    public Optional<Subscription> canceled(StripeWebhookSubscriptionEvent event) {
        log.info("canceled - event:{}", event);
        return subscriptionRepository
                .findByIdAndCustomerId(event.id(), event.customer())
                .map(subs -> {
                    subs.setStatus(Subscription.Status.CANCELED);
                    subs.setEndAt(LocalDateTime.now());
                    return subscriptionRepository.save(subs);
                });
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

    public SubscriptionResponse findByUser(UserEntity user) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(user.getId());

        List<SubscriptionResponseItem> history = subscriptions.stream()
                .filter(s -> !Subscription.isActive(s))
                .map(SubscriptionResponse::toSubscriptionItem)
                .sorted(Comparator.comparing(SubscriptionResponseItem::createdAt).reversed())
                .toList();

        Subscription current = Subscription.findCurrent(subscriptions);

        if (Subscription.isActive(current)) {
            return new SubscriptionResponse(SubscriptionResponse.toSubscriptionItem(current), history);
        }
        return new SubscriptionResponse(null, history);
    }

    public Optional<Subscription> findSubscription(String subscriptionId, Long userId) {
        return subscriptionRepository.findByIdAndUserId(subscriptionId, userId);
    }

    public Optional<Subscription> setEndReasonPastDue(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).map(subs -> {
            subs.setEndReason(Subscription.EndReason.UNPAID);
            return subscriptionRepository.save(subs);
        });
    }

}
