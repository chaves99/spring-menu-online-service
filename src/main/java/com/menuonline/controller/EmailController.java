package com.menuonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.UserEntity;
import com.menuonline.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/qrcode")
    public ResponseEntity<?> qrcode(HttpServletRequest request,
            @RequestParam("qrcode_image") MultipartFile file) throws Exception {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        emailService.sendQrcode(user.getEmail(), file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user-message")
    public ResponseEntity<?> sendUserMessage(@RequestBody UserMessageRequest body) {
        emailService.sendUserMessage(body.userEmail(), body.subject(), body.message());
        return ResponseEntity.ok().build();
    }

    public static record UserMessageRequest(String userEmail, String subject, String message) {
    }

}
