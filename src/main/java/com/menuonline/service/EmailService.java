package com.menuonline.service;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Deprecated // It will not be used for now probably in the future
public class EmailService {

    // @Value("${mailgun.apikey}")
    // private String apiKey;
    //
    // @Value("${mailgun.url}")
    // private String mailgunUrl;

    public void recovery(String emailTo, String token) {
        // String basicAuth = "api:" + apiKey;
        // MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        // parts.add("from", "");
        // parts.add("to", "");
        // // parts.add("subject", subject);
        // // parts.add("text", text);
        // ResponseSpec responseSpec = RestClient
        //         .create()
        //         .post()
        //         .uri(mailgunUrl)
        //         .body(parts)
        //         .headers(h -> {
        //             h.add("Content-Type", "multipart/form-data");
        //             h.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes()));
        //         }).retrieve();
        // System.out.println("response: " + responseSpec.toBodilessEntity());
        // log.info("send = apiKey:{} mailgunUrl:{}", apiKey, mailgunUrl);
    }

}
