package com.menuonline.controller;

import java.time.LocalDateTime;
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
import com.menuonline.entity.Category;
import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<List<CategoryResponse>> create(HttpServletRequest request,
            @RequestBody List<CategoryRequest> body) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        List<Category> list = body.stream()
                .map(c -> new Category(null, c.name(), true, user, null, null, null))
                .toList();
        categoryRepository.saveAll(list);
        return ResponseEntity.ok(findAll(user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        return ResponseEntity.ok(findAll(user.getId()));
    }

    @PutMapping("/disable/{id}")
    @Transactional
    public ResponseEntity<List<CategoryResponse>> disable(HttpServletRequest request,
            @PathVariable Long id) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        log.info("disable - id: {}", id);
        categoryRepository.findByUserIdAndId(user.getId(), id)
            .ifPresent(cat -> {
                cat.setEnabled(!cat.isEnabled());
                categoryRepository.save(cat);
            });
        // categoryRepository.disable(user.getId(), id);
        return ResponseEntity.ok(findAll(user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<CategoryResponse>> delete(HttpServletRequest request,
            @PathVariable Long id) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);
        Category category = categoryRepository
                .findByUserIdAndId(user.getId(), id)
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.NOT_FOUND));
        List<Product> products = productRepository.findByCategoryId(category.getId());
        log.info("delete - category:{} product list:{}", category, products.size());
        productRepository.deleteAll(products);
        categoryRepository.delete(category);
        return ResponseEntity.ok(findAll(user.getId()));
    }

    private List<CategoryResponse> findAll(Long userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return categories.stream().map(CategoryResponse::from).toList();
    }

    public static record CategoryRequest(String name) {
    }

    public static record CategoryResponse(Long id,
            String name,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        public static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(),
                    c.getName(),
                    c.isEnabled(),
                    c.getCreatedAt(),
                    c.getUpdatedAt());
        }
    }

}
