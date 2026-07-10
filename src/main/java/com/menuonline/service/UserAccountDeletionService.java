package com.menuonline.service;

import org.springframework.stereotype.Service;

import com.menuonline.entity.UserEntity;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.PriceRepository;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.ScheduleRepository;
import com.menuonline.repository.TokenAccessRepository;
import com.menuonline.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAccountDeletionService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final TokenAccessRepository tokenAccessRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceRepository priceRepository;
    private final CustomizationService customizationService;

    public void delete(UserEntity user) {

    }
}
