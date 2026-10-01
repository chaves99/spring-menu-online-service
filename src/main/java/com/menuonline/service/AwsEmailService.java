package com.menuonline.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.menuonline.entity.UserEntity;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AwsEmailService {

    private JavaMailSender javaMailSender;
    private ThymeleafTemplateComponent templateComponent;

    private String hostFrom;

    private String companyEmail;

    public AwsEmailService(JavaMailSender javaMailSender,
            ThymeleafTemplateComponent templateComponent,
            @Value("${hostFrom}") String hostFrom,
            @Value("${company-email}") String companyEmail) {
        this.javaMailSender = javaMailSender;
        this.templateComponent = templateComponent;
        this.hostFrom = hostFrom;
        this.companyEmail = companyEmail;
    }

    public void sendToken(String emailTo, String token) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setFrom(hostFrom);
        helper.setTo(emailTo);
        helper.setSubject("Recuperar senha");
        helper.setText(templateComponent.recoveryPassword(token), true);
        javaMailSender.send(mimeMessage);
    }

    public void sendUserMessage(String userEmail, String subject, String message) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(hostFrom);
        msg.setSubject("from: " + userEmail + " - " + subject);
        msg.setText(message);
        msg.setTo(companyEmail);
        javaMailSender.send(msg);
    }

    private void send(String to, String subject, String body) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                true,
                StandardCharsets.UTF_8.name());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(hostFrom);
        helper.setText(body, true);

        javaMailSender.send(mimeMessage);
    }

    public void sendQrcode(UserEntity user, MultipartFile file) throws Exception {
        String body = """
                Ola!,

                Aqui esta seu ItiMenu QR Code:

                Obrigado, ItiMenu.
                """;
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                true,
                StandardCharsets.UTF_8.name());
        helper.setFrom(hostFrom);
        helper.setTo(user.getEmail());
        helper.setSubject("Seu ItiMenu QR Code chegou!");
        helper.setText(body, false);
        helper.addAttachment(user.getEstablishmentName().replaceAll("\s", "_") + "_qrcode.png", file);

        javaMailSender.send(mimeMessage);
    }

    public void subscriptionCancel(String emailTo) throws MessagingException {
        this.send(emailTo, "Assinatura cancelada.", templateComponent.cancelSubscription());
    }

    public void sendPastDuePayment(String emailTo) throws MessagingException {
        this.send(emailTo, "Erro no pagamento(Assinatura cancelada).", templateComponent.paymentPastDue());
    }

    public void sendAccountCreation(UserEntity user) throws MessagingException {
        Map<String, Object> params = new HashMap<>();
        params.put("user_name", user.getEstablishmentName());
        params.put("company_name", "ItiMenu");
        params.put("company_name", "ItiMenu");
        this.send(user.getEmail(), "Seja bem vindo ao ItiMenu.", templateComponent.accountCreation(params));
    }

    public void sendDeleteAccount(String emailTo) throws MessagingException {
        this.send(emailTo, "Exclusão de conta ItiMenu.", templateComponent.deleteAccount(emailTo));
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

        public String deleteAccount(String username) {
            Map<String, Object> params = new HashMap<>();
            params.put("user_name", username);
            return process("delete_account", Map.of());
        }

        public String accountCreation(Map<String, Object> map) {
            return process("account_creation", map);
        }

        private String process(String templateName, Map<String, Object> variables) {
            final Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        }
    }
}
