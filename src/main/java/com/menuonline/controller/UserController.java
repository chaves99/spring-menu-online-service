package com.menuonline.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.CreateUserRequest;
import com.menuonline.payloads.LoginUserRequest;
import com.menuonline.payloads.LoginUserResponse;
import com.menuonline.payloads.ResetPasswordRequest;
import com.menuonline.payloads.UpdatePasswordRequest;
import com.menuonline.service.EmailService;
import com.menuonline.service.MockMenuService;
import com.menuonline.service.SimpleStorageBucketSerivce;
import com.menuonline.service.ThymeleafTemplateService;
import com.menuonline.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SimpleStorageBucketSerivce bucketSerivce;
    private final MockMenuService mockMenuService;
    private final EmailService emailService;
    private final ThymeleafTemplateService thymeleafTemplateService;

    @PostMapping
    @Transactional
    public ResponseEntity<LoginUserResponse> create(@RequestBody CreateUserRequest request) {
        UserEntity userEntity = userService.create(request);
        mockMenuService.create(userEntity);
        TokenAccess login = userService.login(userEntity);
        return ResponseEntity.ok(LoginUserResponse.from(login));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(@RequestBody LoginUserRequest request) {
        TokenAccess login = userService.login(request);
        return ResponseEntity.ok(LoginUserResponse.from(login));
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(HttpServletRequest request, @RequestBody UpdatePasswordRequest body) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        userService.updatePassword(user, body);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/generate-token/{email}")
    @Transactional
    public ResponseEntity<?> generateToken(@PathVariable String email) {
        String token = userService.generateRecoveryToken(email);
        String recoveryPasswordTemplate = thymeleafTemplateService.recoveryPassword(token);
        emailService.sendToken(email, recoveryPasswordTemplate);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> body) {
        if(userService.validateRecoveryToken(body.get("email"), body.get("token"))) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest body) {
        System.out.println("#### resetPassword body:" + body);
        userService.resetPassword(body);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<LoginUserResponse> get(HttpServletRequest request) {
        String token = (String) request.getAttribute(AuthFilter.TOKEN_ATTR_KEY);
        return userService.get(token)
                .map(t -> ResponseEntity.ok(LoginUserResponse.from(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/image")
    public ResponseEntity<LoginUserResponse> putImage(HttpServletRequest request,
            @RequestParam("image_file") MultipartFile file) throws IOException {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        String token = (String) request.getAttribute(AuthFilter.TOKEN_ATTR_KEY);
        String imageUrl = bucketSerivce.uploadEstablishment(user, file);
        return ResponseEntity.ok(LoginUserResponse.from(user, token, imageUrl));
    }

}
