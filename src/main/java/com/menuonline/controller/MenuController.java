package com.menuonline.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.entity.Subscription;
import com.menuonline.service.MenuService;
import com.menuonline.service.SubscriptionService;
import com.menuonline.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("menu")
@RequiredArgsConstructor
public class MenuController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final MenuService menuService;

    @GetMapping("{establishmentUrl}")
    public ResponseEntity<?> get(@PathVariable String establishmentUrl) {
        return userService
                .findByUrl(establishmentUrl)
                .flatMap(user -> {
                    subscriptionService.verifyUserFreeTier(user);
                    var active = user.getSubscriptions().stream()
                            .anyMatch(s -> s.getStatus().equals(Subscription.Status.ACTIVE));
                    if (!active) {
                        return Optional.empty();
                    }
                    return menuService.get(user);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
