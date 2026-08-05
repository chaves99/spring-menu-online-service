package com.menuonline.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Customization;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.repository.CustomizationRepository;
import com.menuonline.service.CustomizationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customization")
@RequiredArgsConstructor
public class CustomizationController {

    private final CustomizationService customizationService;
    private final CustomizationRepository customizationRepository;

    @GetMapping
    public ResponseEntity<?> get(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return findAll(user);
    }

    private ResponseEntity<?> findAll(UserEntity user) {
        List<Customization> customizations = customizationRepository.findByUserId(user.getId());
        if (customizations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customizations.stream().map(CustomizationPayload::from).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return ResponseEntity.ok(CustomizationPayload.from(customizationRepository.findActive(user.getId())));
    }

    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody CustomizationPayload body) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return ResponseEntity.ok(CustomizationPayload.from(customizationService.newCustomization(user, body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(HttpServletRequest request, @PathVariable Long id) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return customizationRepository.findById(id)
                .map(theme -> {
                    if (theme.getActive()) {
                        throw new HttpServiceException("Cannot delete active customization", null, HttpStatus.CONFLICT);
                    }
                    boolean equals = theme.getUser().equals(user);
                    if (equals) {
                        customizationRepository.delete(theme);
                        return findAll(user);
                    }
                    return ResponseEntity.notFound().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static record CustomizationPayload(Long id, String name, String mainColor, String secondaryColor,
            String font,
            String theme, boolean builtin, boolean active) {
        public static CustomizationPayload from(Customization c) {
            return new CustomizationPayload(c.getId(), c.getName(), c.getMainColor(), c.getSecondaryColor(),
                    c.getFont(),
                    c.getThemeType().name(), c.getBuiltin(), c.getActive());
        }
    }
}
