package com.menuonline.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThymeleafTemplateService {

    private final TemplateEngine templateEngine;

    public String recoveryPassword(String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        return process("recovery_password", params);
    }

    private String process(String templateName, Map<String, Object> variables) {
        final Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
