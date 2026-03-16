package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.menuonline.entity.Schedule;
import com.menuonline.entity.Subscription;
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

    public Optional<CustomerMenuResponse> get(String establishmentUrl) {
        log.info("get - establishmentUrl:{}", establishmentUrl);
        try {
            UserEntity info = userRepository.findByEstablishmentUrl(establishmentUrl)
                    .orElseThrow(() -> new HttpServiceException(ErrorMessages.ESTABLISHMENT_NOT_EXISTS,
                            HttpStatus.NOT_FOUND));

            if (!isUserSubscriptionValid(info)) {
                throw new HttpServiceException(null, HttpStatus.NOT_FOUND);
            }

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

    private boolean isUserSubscriptionValid(UserEntity user) {
        Optional<Subscription> current = Subscription.findCurrent(user.getSubscriptions());
        if (current.isEmpty()) {
            return false;
        }
        Subscription subs = current.get();

        if (subs.getFreeTier() && subs.getEndAt().isAfter(LocalDateTime.now())) {
            return true;
        }

        if (!subs.getFreeTier() && subs.getStatus().equals(Subscription.Status.ACTIVE)) {
            return true;
        }

        return false;
    }

}
