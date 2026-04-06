package com.menuonline.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.menuonline.facade.stripe.SubscriptionEventFacade;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;
import com.stripe.net.Webhook;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Controller
@Slf4j
@RequestMapping("/stripe/webhook")
public class StripeWebhookController {

    private final String webhookSecret;

    private final ObjectMapper objectMapper;

    private final SubscriptionEventFacade eventFacade;

    public StripeWebhookController(@Value("${stripe.webhook.secret}") String webhookSecret,
            ObjectMapper objectMapper,
            SubscriptionEventFacade eventFacade) {
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
        this.eventFacade = eventFacade;
    }

    @PostMapping
    public ResponseEntity<?> webhook(@RequestBody String payload, @RequestHeader HttpHeaders headers) {
        try {
            log.info("webhook - payload:{}", payload);
            String stripeSignature = headers.getFirst("Stripe-Signature");
            Webhook.constructEvent(payload, stripeSignature, webhookSecret);

            StripeWebhookEvent event = objectMapper.readValue(payload, StripeWebhookEvent.class);
            String type = event.type();
            switch (type) {
                case "customer.subscription.created":
                    eventFacade.newSubscription(event.data().object());
                    break;
                case "customer.subscription.deleted":
                    eventFacade.cancelSubscription(event.data().object());
                    break;
                case "customer.subscription.updated":
                    eventFacade.syncSubscription(event.data().object());
                    break;
                default:
                    log.info("webhook - unhandle type:{} event:{} dataJson:{}",
                            event.type(), event, payload);
                    break;
            }
        } catch (Exception e) {
            log.error("webhook - exception:", e);
        }
        return ResponseEntity.ok().build();
    }

    public static record StripeWebhookEvent(String id, StripeData data, String type) {
        public static record StripeData(StripeWebhookSubscriptionEvent object) {
        };
    }

}
