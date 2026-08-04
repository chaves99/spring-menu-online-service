package com.menuonline.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.controller.CustomizationController.CustomizationPayload;
import com.menuonline.entity.Customization;
import com.menuonline.entity.Customization.Theme;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.CustomizationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomizationService {

    private final CustomizationRepository customizationRepository;

    @Transactional
    public Customization newCustomization(UserEntity user, CustomizationPayload body) {
        Customization active = customizationRepository.findActive(user.getId());
        active.setActive(false);
        Customization entity = new Customization(null, body.name(), body.mainColor(), body.secondaryColor(),
                body.font(), Customization.Theme.from(body.theme()), user, true, false);
        return customizationRepository.save(entity);
    }

    public void initDefault(UserEntity user) {
        Customization custom1 = new Customization(null, "Padrão Claro", "#ffffff", "#e9ecef", "Google Sans",
                Theme.LIGHT, user, true, true);
        Customization custom2 = new Customization(null, "Padrão Escuro", "#343a40", "#495057", "Google Sans",
                Theme.DARK, user, false, true);
        customizationRepository.saveAll(List.of(custom1, custom2));
    }
}
