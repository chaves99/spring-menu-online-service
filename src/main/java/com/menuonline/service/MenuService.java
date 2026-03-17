package com.menuonline.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.CustomerMenuResponse;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.ProductRepository.ProductMenuProjection;
import com.menuonline.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {

    private final ScheduleRepository scheduleRepository;

    private final ProductRepository productRepository;

    public Optional<CustomerMenuResponse> get(UserEntity user) {
        log.info("get - user:{}", user);
        try {
            List<Schedule> schedules = scheduleRepository.findByUserId(user.getId());

            List<ProductMenuProjection> productProjections = productRepository.findMenu(user.getId());
            return Optional.of(CustomerMenuResponse.from(user, schedules, productProjections));
        } catch (HttpServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("get - Exception: ", e);
            return Optional.empty();
        }
    }

}
