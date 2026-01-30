package com.menuonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("{establishmentName}")
    public ResponseEntity<?> get(@PathVariable String establishmentName) {
        return menuService.get(establishmentName)
            .map(obj -> ResponseEntity.ok(obj))
            .orElse(ResponseEntity.notFound().build());
    }
}
