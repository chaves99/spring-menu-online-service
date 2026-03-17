package com.menuonline.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.menuonline.payloads.stripe.StripeWebhookInvoice;
import com.menuonline.payloads.stripe.StripeWebhookSubscriptionCancelled;
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
    public ResponseEntity<?> webhook(@RequestBody Map<String, Object> body, @RequestHeader HttpHeaders headers) {
        String type = (String) body.get("type");
        // String stripeSignature = headers.getFirst("Stripe-Signature");
        log.info("webhook - event type:{}", type);
        String dataJson = objectMapper.writeValueAsString(body.get("data"));
        log.info("webhook - body:{}", dataJson);

        switch (type) {
            case "invoice.paid":
                StripeWebhookInvoice invoicePaid = objectMapper.readValue(dataJson, StripeWebhookInvoice.class);
                subscriptionService.updateSubs(invoicePaid);
                break;
            case "invoice.payment_failed":
                StripeWebhookInvoice paymentFailed = objectMapper.readValue(dataJson, StripeWebhookInvoice.class);
                subscriptionService.paymentFail(paymentFailed);

                break;
            case "customer.subscription.deleted":
                StripeWebhookSubscriptionCancelled subsCancelled = objectMapper.readValue(dataJson, StripeWebhookSubscriptionCancelled.class);
                subscriptionService.cancelled(subsCancelled);
                break;
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/simulate/{email}")
    public ResponseEntity<?> webhook(@PathVariable String email) {
        return ResponseEntity.ok().build();
    }

    public static record StripeWebhookSubscriptionInfo(String id) {
    }

}
