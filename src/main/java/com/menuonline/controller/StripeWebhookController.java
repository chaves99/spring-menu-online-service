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
        log.info("webhook - body:{}", objectMapper.writeValueAsString(body.get("data")));


        // switch (type) {
        //     case "invoice.paid":
        //          paidPayload = parsePayload(body.get("data"));
        //         subscriptionService.updateSubs((String) body.get("customer_email"),
        //                 (String) body.get("customer"), paidPayload);
        //         break;
        //     case "invoice.payment_failed":
        //         StripeSubscriptionResponsePayload failedPayload = parsePayload(body.get("data"));
        //         subscriptionService.paymentFailed((String) body.get("customer_email"),
        //                 (String) body.get("customer"), failedPayload);
        //         break;
        // }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/simulate/{email}")
    public ResponseEntity<?> webhook(@PathVariable String email) {
        return ResponseEntity.ok().build();
    }

    public static record StripeWebhookSubscriptionInfo(String id){}

}
