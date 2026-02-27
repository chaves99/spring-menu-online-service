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
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    @Value("${mailgun.hostFrom}")
    private String hostFrom;

    @Value("${mailgun.apikey}")
    private String apiKey;

    @Value("${mailgun.url}")
    private String mailgunUrl;

    private final RestClient.Builder restClientBuilder;

    public void sendToken(String emailTo, String htmlTemplate) {
        try {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("from", hostFrom);
            parts.add("to", emailTo);
            parts.add("subject", "Recuperar senha");
            parts.add("html", htmlTemplate);

            ResponseSpec responseSpec = getClient().post().body(parts).retrieve();
            log.info("sendToken - success:{}", responseSpec.toEntity(String.class).getStatusCode().is2xxSuccessful());
        } catch (Exception e) {
            log.warn("sendToken - exception: ", e);
            throw e;
        }
    }

    private RestClient getClient() {
        String basicAuth = "api:" + apiKey;
        return restClientBuilder
                .baseUrl("https://api.mailgun.net/v3/" + mailgunUrl + "/messages")
                .defaultHeaders(h -> {
                    h.add("Content-Type", "multipart/form-data");
                    h.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes()));
                })
                .build();

    }

    public void qrcode(String emailTo, byte[] file) {
    }

}
