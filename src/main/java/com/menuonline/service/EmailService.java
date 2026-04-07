package com.menuonline.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Value("${mailgun.hostFrom}")
    private String hostFrom;

    private final RestClient restClient;

    private final ThymeleafTemplateComponent templateComponent;

    public EmailService(RestClient.Builder restClientBuilder,
            ThymeleafTemplateComponent templateComponent,
            @Value("${mailgun.url}") String mailgunUrl,
            @Value("${mailgun.apikey}") String apiKey) {
        this.templateComponent = templateComponent;
        String basicAuth = "api:" + apiKey;
        this.restClient = restClientBuilder
                .baseUrl("https://api.mailgun.net/v3/" + mailgunUrl + "/messages")
                .defaultHeaders(h -> {
                    h.add("Content-Type", "multipart/form-data");
                    h.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes()));
                })
                .build();
    }

    public void sendToken(String emailTo, String token) {
        try {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("from", hostFrom);
            parts.add("to", emailTo);
            parts.add("subject", "Recuperar senha");
            parts.add("html", templateComponent.recoveryPassword(token));

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

    public void subscriptionCancel(String emailTo) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("from", hostFrom);
        parts.add("to", emailTo);
        parts.add("subject", "Assinatura cancelada.");
        parts.add("html", templateComponent.cancelSubscription());

        send(parts);
    }

    public void sendPastDuePayment(String emailTo) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("from", hostFrom);
        parts.add("to", emailTo);
        parts.add("subject", "Erro no pagamento(Assinatura cancelada).");
        parts.add("html", templateComponent.paymentPastDue());

        send(parts);
    }

    public void sendUserMessage(String userEmail, String subject, String message) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("from", hostFrom);
        parts.add("to", hostFrom);
        parts.add("subject", "User Message");
        parts.add("text", message);

        send(parts);
    }

    @Component
    @RequiredArgsConstructor
    public static class ThymeleafTemplateComponent {

        private final TemplateEngine templateEngine;

        public String recoveryPassword(String token) {
            Map<String, Object> params = new HashMap<>();
            params.put("token", token);
            return process("recovery_password", params);
        }

        public String paymentPastDue() {
            return process("payment_past_due_cancel", Map.of());
        }

        public String cancelSubscription() {
            return process("cancel_subscription", Map.of());
        }

        private String process(String templateName, Map<String, Object> variables) {
            final Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        }
    }
}
