package com.menuonline.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.TokenAccessRepository;
import com.menuonline.service.MenuService;
import com.menuonline.service.SubscriptionService;
import com.menuonline.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final MenuService menuService;
    private final TokenAccessRepository tokenAccessRepository;

    @GetMapping("{establishmentUrl}")
    public ResponseEntity<?> get(@RequestHeader(value = AuthFilter.AUTH_HEADER, required = false) String token,
            @PathVariable String establishmentUrl) {
        Optional<UserEntity> userOpt = token != null
            ? tokenAccessRepository.findById(token).map(t -> t.getUser())
            : Optional.empty();
        return userService
                .findByUrl(establishmentUrl)
                .flatMap(user -> {
                    log.info("get - establishmentUrl:{} user:{}", establishmentUrl, user);
                    if (userOpt.isPresent()
                            && userOpt.get().getEstablishmentUrl().equals(establishmentUrl)) {
                        // Logged user is accessing own menu
                        return menuService.get(user);
                    }
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
