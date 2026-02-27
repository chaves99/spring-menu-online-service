package com.menuonline.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Controller
@Slf4j
@RequestMapping("/stripe/webhook")
public class StripeWebhookController {

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<?> webhook(@RequestBody Map<String, Object>  body, @RequestHeader HttpHeaders headers) {
        if (body.get("type").equals("invoice.paid")) {
            log.info("stripe webhook - httpHeaders:{} body:{}", headers, objectMapper.writeValueAsString(body.get("data")));
        }
        return ResponseEntity.ok().build();
    }

}
