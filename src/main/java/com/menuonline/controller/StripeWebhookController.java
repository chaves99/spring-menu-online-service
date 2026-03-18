package com.menuonline.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.menuonline.facade.stripe.SubscriptionEventFacade;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Controller
@Slf4j
@RequestMapping("/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final ObjectMapper objectMapper;

    private final SubscriptionEventFacade eventFacade;

    @PostMapping
    public ResponseEntity<?> webhook(@RequestBody StripeWebhookEvent event, @RequestHeader HttpHeaders headers) {
        try {
            String type = event.type();
            String stripeSignature = headers.getFirst("Stripe-Signature");
            String dataJson = objectMapper.writeValueAsString(event.data().get("object"));
            switch (type) {
                case "customer.subscription.created":
                    StripeWebhookSubscriptionEvent created = objectMapper.readValue(dataJson,
                            StripeWebhookSubscriptionEvent.class);
                    eventFacade.newSubscription(created);
                    break;
                case "customer.subscription.deleted":
                    StripeWebhookSubscriptionEvent canceled = objectMapper.readValue(dataJson,
                            StripeWebhookSubscriptionEvent.class);
                    eventFacade.cancelSubscription(canceled);
                    break;
                default:
                    log.info("webhook - unhandle type:{} event:{}", event.type(), event);
                    break;
            }
        } catch (Exception e) {
            log.error("webhook - exception:", e);
        }
        return ResponseEntity.ok().build();
    }

    public static record StripeWebhookEvent(String id, Map<String, Object> data, String type) {
    }

}
