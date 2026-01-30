package com.menuonline.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.CustomerMenuResponse;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.ScheduleRepository;
import com.menuonline.repository.ProductRepository.ProductMenuProjection;
import com.menuonline.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {

    private final UserRepository userRepository;

    private final ScheduleRepository scheduleRepository;

    private final ProductRepository productRepository;

    public Optional<CustomerMenuResponse> get(String establishment) {
        log.info("get - establishment:{}", establishment);
        try {
            UserEntity info = userRepository.findByEstablishmentName(establishment)
                    .orElseThrow(() -> new HttpServiceException(ErrorMessages.ESTABLISHMENT_NOT_EXISTS,
                            HttpStatus.NOT_FOUND));

            List<Schedule> schedules = scheduleRepository.findByUserId(info.getId());

            List<ProductMenuProjection> productProjections = productRepository.findMenu(info.getId());
            return Optional.of(CustomerMenuResponse.from(info, schedules, productProjections));
        } catch (HttpServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("get - Exception: ", e);
            return Optional.empty();
        }
    }

}
