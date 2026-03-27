package com.menuonline.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.SubscriptionResponse;
import com.menuonline.service.EmailService;
import com.menuonline.service.StripeService;
import com.menuonline.service.SubscriptionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<SubscriptionResponse> get(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        SubscriptionResponse subsResponse = subscriptionService.findByUser(user);
        return ResponseEntity.ok(subsResponse);
    }

    @GetMapping("/{subscriptionId}/details")
    public ResponseEntity<?> getDetails(HttpServletRequest request, @PathVariable String subscriptionId) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return subscriptionService
                .findSubscription(subscriptionId, user.getId())
                .flatMap(subscription -> stripeService.findDetails(subscription.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<?> cancel(HttpServletRequest request, @PathVariable String subscriptionId) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        Subscription subscription = user.getSubscriptions().stream()
                .filter(s -> s.getId().equals(subscriptionId))
                .findFirst().orElseThrow(() -> new HttpServiceException(null, HttpStatus.UNAUTHORIZED));

        stripeService.cancel(subscription.getId());
        subscriptionService.cancel(user, subscription);
        try {
            emailService.subscriptionCancel(user.getEmail());
        } catch (Exception e) {
            log.warn("cancel - error on sending email:{} exception:{} ",
                    user.getEmail(), e.getMessage());
        }
        return ResponseEntity.ok(subscriptionService.findByUser(user));
    }

    @GetMapping("/update-payment-method/{subscriptionId}")
    public ResponseEntity<String> getPaymentMethodChangeUrl(HttpServletRequest request,
            @PathVariable String subscriptionId) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return subscriptionService
                .findSubscription(subscriptionId, user.getId())
                .flatMap(subs -> {
                    return stripeService.generateChangePaymentMethodUrl(subs.getCustomerId(), subs.getId());
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/new-plan")
    public ResponseEntity<String> getNewPlanUrl(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return stripeService.generateNewPlanUrl(user.getEmail())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.internalServerError().build());
    }

}
