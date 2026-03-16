package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
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
        subs.setUser(user);
        subs.setDescription("FREE TIER");
        subs.setFreeTier(true);
        subs.setEndDate(LocalDateTime.now().plusDays(freeTierDays));
        subs.setStatus(Subscription.Status.ACTIVE);
        subs.setId("u_" + user.getId() + "_" + TokenGeneratorUtil.generate(20));
        return subscriptionRepository.save(subs);
    }

    public Subscription findCurrent(UserEntity user) {
        return subscriptionRepository.findCurrent(user.getId());
    }

    public void updateSubs(String email, String customerId, String subscriptionId) {
        log.info("updateSubs - email:{} customerId:{}", email, customerId);
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("updateSubs - not found - email:{} customerId:{}", email, customerId);
            return;
        }

        UserEntity user = userOpt.get();
        user.getSubscriptions().stream()
                .filter(s -> s.getId().equals(subscriptionId))
                .findAny()
                .ifPresentOrElse(
                        subs -> {
                            // subs.setEndDate(stripeSubscription.getEndedAt());
                            subs.setStatus(Subscription.Status.ACTIVE);
                            log.info("updateSubs - subs:{}", subs);
                            subscriptionRepository.save(subs);
                        },
                        () -> {
                            Subscription subs = new Subscription();
                            subs.setStatus(Subscription.Status.ACTIVE);
                            subs.setEndDate(stripeSubscription.getEndedAt());
                            subs.setCustomerId(customerId);
                            subs.setId(stripeSubscription.id());
                            subs.setUser(user);
                            log.info("updateSubs - new subs:{}", subs);
                            subscriptionRepository.save(subs);
                        });
    }

    public void paymentFailed(String email, String customerId,
            String subscriptionId) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("updateSubs - not found - email:{} customerId:{}", email, customerId);
            return;
        }

        userOpt.get().getSubscriptions().stream()
                .filter(s -> s.getId().equals(subscriptionId))
                .findAny()
                .ifPresent(subs -> {
                    subs.setStatus(Subscription.Status.PAYMENT_FAILED);
                    log.info("paymentFailed - updating subs: {}", subs);
                    subscriptionRepository.save(subs);
                });
    }

    public Subscription cancel(UserEntity user, Subscription subscription) {
        subscription.setStatus(Subscription.Status.CANCELLED);
        return subscriptionRepository.save(subscription);
    }

}
