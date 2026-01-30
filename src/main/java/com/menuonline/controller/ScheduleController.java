package com.menuonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;
import com.menuonline.payloads.ScheduleRequest;
import com.menuonline.payloads.ScheduleResponse;
import com.menuonline.repository.ScheduleRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository repository;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> get(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return findByUser(user);
    }

    private ResponseEntity<List<ScheduleResponse>> findByUser(UserEntity user) {
        return ResponseEntity.ok(repository
                .findByUserId(user.getId()).stream().map(ScheduleResponse::from).toList());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<List<ScheduleResponse>> create(HttpServletRequest request,
            @RequestBody List<ScheduleRequest> body) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        List<Schedule> beDeleted = repository.findByUserId(user.getId());
        repository.deleteAll(beDeleted);
        List<Schedule> list = body.stream().map(s -> s.toEntity(user)).toList();
        repository.saveAll(list);
        return findByUser(user);
    }

}
