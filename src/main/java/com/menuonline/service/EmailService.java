package com.menuonline.service;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Value("${mailgun.hostFrom}")
    private String hostFrom;

    private final RestClient restClient;

    public EmailService(RestClient.Builder restClientBuilder,
            @Value("${mailgun.url}") String mailgunUrl,
            @Value("${mailgun.apikey}") String apiKey) {
        String basicAuth = "api:" + apiKey;
        this.restClient = restClientBuilder
                .baseUrl("https://api.mailgun.net/v3/" + mailgunUrl + "/messages")
                .defaultHeaders(h -> {
                    h.add("Content-Type", "multipart/form-data");
                    h.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes()));
                })
                .build();
    }

    public void sendToken(String emailTo, String htmlTemplate) {
        try {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("from", hostFrom);
            parts.add("to", emailTo);
            parts.add("subject", "Recuperar senha");
            parts.add("html", htmlTemplate);

            ResponseSpec responseSpec = send(parts);
            log.info("sendToken - success:{}", responseSpec.toEntity(String.class).getStatusCode().is2xxSuccessful());
        } catch (Exception e) {
            log.warn("sendToken - exception: ", e);
            throw e;
        }
    }

    private ResponseSpec send(MultiValueMap<String, Object> parts) {
        return restClient.post().body(parts).retrieve();

    }

    public void sendQrcode(String emailTo, MultipartFile file) throws Exception {
        try {
            var resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            };

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("from", hostFrom);
            parts.add("to", emailTo);
            parts.add("subject", "Seu ItiMenu QR Code chegou!");
            parts.add("attachment", resource);
            parts.add("text", "Aqui esta seu QR Code:");

            ResponseSpec responseSpec = send(parts);
            log.info("sendQrcode - success:{}", responseSpec.toEntity(String.class).getStatusCode().is2xxSuccessful());
        } catch (Exception e) {
            log.warn("sendQrcode - exception: ", e);
            throw e;
        }
    }

    public void sendUserMessage(String userEmail, String subject, String message) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("from", hostFrom);
        parts.add("to", hostFrom);
        parts.add("subject", "User Message");
        parts.add("text", message);

        send(parts);
    }

    public void subscriptionCancel(String emailTo) throws Exception {
    }

}
