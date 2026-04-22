package com.menuonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customization")
public class CustomizationController {

    private List<Customization> customs = List.of(
            new Customization("#d79921", "#98971a", "#282828", "#fbf1c7"), // DARK GRUVBOX
            new Customization("#d79921", "#98971a", "#fbf1c7", "#282828") // LIGHT GRUVBOX
    );

    @GetMapping
    public ResponseEntity<?> get() {
        return null;
    }

    public static record Customization(String mainColor,
            String secondaryColor,
            String backgroundColor,
            String textColor) {
    }
}
