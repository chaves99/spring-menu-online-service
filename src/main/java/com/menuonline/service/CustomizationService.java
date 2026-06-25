package com.menuonline.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.menuonline.entity.Customization;
import com.menuonline.entity.UserEntity;
import com.menuonline.entity.Customization.Theme;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.repository.CustomizationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomizationService {

    private final CustomizationRepository customizationRepository;

    @Transactional
    public void setActive(UserEntity user, @PathVariable Long id) {
        Optional<Customization> opt = customizationRepository.findById(id);
        if (opt.isEmpty() || !opt.get().getUser().equals(user)) {
            throw HttpServiceException.notFound();
        }
        List<Customization> byUserId = customizationRepository
                .findByUserId(user.getId());
        byUserId
                .forEach(c -> c.setActive(Boolean.FALSE));

        opt.get().setActive(Boolean.TRUE);
    }

    public void initDefault(UserEntity user) {
        Customization custom1 = new Customization(null, "Padrão Claro", "#ffffff", "#e9ecef", "Google Sans",
                Theme.LIGHT, user, true, true);
        Customization custom2 = new Customization(null, "Padrão Escuro", "#343a40", "#495057", "Google Sans",
                Theme.DARK, user, false, true);
        customizationRepository.saveAll(List.of(custom1, custom2));
    }
}
