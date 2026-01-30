package com.menuonline.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("address")
@RequiredArgsConstructor
public class AddressController {

    private final UserRepository UserRepository;
    
    @GetMapping
    public ResponseEntity<AddressPayload> get(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return ResponseEntity.ok(new AddressPayload(user.getCode(), user.getCity(), user.getAddressLine()));
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void create(HttpServletRequest request,
            @RequestBody AddressPayload body) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        UserRepository.updateAddress(user.getId(), body.code(), body.line(), body.city());
    }

    public static record AddressPayload(String code, String city, String line){}

}
