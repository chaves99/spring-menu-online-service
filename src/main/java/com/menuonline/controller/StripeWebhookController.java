package com.menuonline.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/stripe/webhook")
public class StripeWebhookController {

    @PostMapping
    public ResponseEntity<?> webhook(@RequestBody String body, @RequestHeader HttpHeaders headers) {
        log.info("stripe webhook - httpHeaders:{} body:{}", headers, body);
        return ResponseEntity.ok().build();
    }

}
