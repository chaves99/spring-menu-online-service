package com.menuonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/contact")
@RestController
@AllArgsConstructor
@Slf4j
public class ContactController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ContactRecord> get(@RequestAttribute(AuthFilter.USER_ATTR_KEY) UserEntity user) {
        return ResponseEntity.ok(new ContactRecord(user.getInstagram(), user.getFacebook(),
                user.getWebsite(), user.getPhone(), user.getWhatsapp()));
    }

    @PostMapping
    public ResponseEntity<ContactRecord> post(@RequestAttribute(AuthFilter.USER_ATTR_KEY) UserEntity user,
            @RequestBody ContactRecord body) {
        user.setWhatsapp(body.whatsapp());
        user.setWebsite(body.website());
        user.setFacebook(body.facebook());
        user.setInstagram(body.instagram());
        user.setPhone(body.phone());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    public static record ContactRecord(String instagram, String facebook,
            String website, String phone, String whatsapp) {
    }
}
