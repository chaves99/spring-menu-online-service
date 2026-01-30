package com.menuonline.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.payloads.CreateProductRequest;
import com.menuonline.payloads.ProductResponse;
import com.menuonline.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/product")
public class ProductController {

    private final ProductService service;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> get(HttpServletRequest request,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "20", required = false) int size,
            ProductFilterParamRequest filter) {
        log.info("get - filter: {}", filter);
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return ResponseEntity.ok(service.getAll(user, page, size, filter));
    }

    public static record ProductFilterParamRequest(String name, Boolean active, Long categoryId) {
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(HttpServletRequest request,
            @PathVariable Long id) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        Product product = service.getById(id, user.getId())
                .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PostMapping
    public ResponseEntity<List<ProductResponse>> register(@RequestBody CreateProductRequest body) {
        service.create(body);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<List<ProductResponse>> update(@PathVariable Long id, @RequestBody CreateProductRequest body) {
        service.update(id, body);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> toggleActive(HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "20", required = false) int size) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        service.toggleActive(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "20", required = false) int size) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        service.delete(id, user);
        return ResponseEntity.ok().build();
    }

}
