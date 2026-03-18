package com.menuonline.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.menuonline.payloads.stripe.StripeWebhookInvoice;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.menuonline.service.StripeService;
import com.menuonline.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Controller
@Slf4j
@RequestMapping("/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final ObjectMapper objectMapper;

    private final SubscriptionService subscriptionService;
    private final StripeService stripeService;

    @PostMapping
    public ResponseEntity<?> webhook(@RequestBody StripeWebhookEvent event, @RequestHeader HttpHeaders headers) {
        try {
            String type = event.type();
            String stripeSignature = headers.getFirst("Stripe-Signature");
            log.info("webhook - event type:{} stripeSignature:{}", type, stripeSignature);
            String dataJson = objectMapper.writeValueAsString(event.data().get("object"));
            log.info("webhook - body:{}", dataJson);

            if (type.equals("customer.subscription.created")) {
                StripeWebhookSubscriptionEvent created = objectMapper.readValue(dataJson,
                        StripeWebhookSubscriptionEvent.class);
                Optional<String> email = stripeService.findEmailByCustomer(created.customer());
                if (email.isPresent()) {
                    subscriptionService.createSubscription(created, email.get());
                } else {
                    log.warn("customer not found - customer:{}", created.customer());
                }

            } else if (type.equals("customer.subscription.deleted")) {
                StripeWebhookSubscriptionEvent canceled = objectMapper.readValue(dataJson,
                        StripeWebhookSubscriptionEvent.class);
                Optional<String> email = stripeService.findEmailByCustomer(canceled.customer());
                if (email.isPresent()) {
                    subscriptionService.canceled(canceled);
                } else {
                    log.warn("customer not found - customer:{}", canceled.customer());
                }
            } else if (type.equals("invoice.payment_failed")) {
                StripeWebhookInvoice paymentFailed = objectMapper.readValue(dataJson, StripeWebhookInvoice.class);
                log.info("invoice payment fail - StripeWebhookInvoice:{}", paymentFailed);
            } else {
                log.info("webhook - unhandle type:{} event:{}",  event.type(), event);
            }
        } catch (Exception e) {
            log.error("webhook - exception:", e);
        }
        return ResponseEntity.ok().build();
    }

    public static record StripeWebhookEvent(String id, Map<String, Object> data, String type) {
    }

}
