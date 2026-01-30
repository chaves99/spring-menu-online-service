package com.menuonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.CreateUserRequest;
import com.menuonline.payloads.LoginUserRequest;
import com.menuonline.payloads.LoginUserResponse;
import com.menuonline.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Transactional
    public ResponseEntity<LoginUserResponse> create(@RequestBody CreateUserRequest request) {
        UserEntity userEntity = userService.create(request);
        TokenAccess login = userService.login(userEntity);
        return ResponseEntity.ok(LoginUserResponse.from(login));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(@RequestBody LoginUserRequest request) {
        TokenAccess login = userService.login(request);
        return ResponseEntity.ok(LoginUserResponse.from(login));
    }

    @GetMapping
    public ResponseEntity<LoginUserResponse> get(HttpServletRequest request) {
        String token = (String) request.getAttribute(AuthFilter.TOKEN_ATTR_KEY);
        return userService.get(token)
                .map(t -> ResponseEntity.ok(LoginUserResponse.from(t)))
                .orElse(ResponseEntity.notFound().build());
    }

}
