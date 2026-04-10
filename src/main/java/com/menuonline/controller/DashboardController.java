package com.menuonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Category;
import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboard")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> get(HttpServletRequest request) {
        UserEntity user = (UserEntity) request.getAttribute(AuthFilter.USER_ATTR_KEY);

        List<Category> categories = categoryRepository.findByUserIdOrderBySequence(user.getId());
        List<Product> products = productRepository.findByCategoryIdIn(
                categories.stream().map(Category::getId).toList());

        long activeProducts = products.stream().filter(p -> p.isActive()).count();
        long inactiveProdcts = products.size() - activeProducts;

        long activeCategories = categories.stream().filter(c -> c.isEnabled()).count();
        long inactiveCategories = categories.size() - activeCategories;

        return ResponseEntity.ok(new DashboardResponse(
                new DashboardResponse.DashboardProduct(activeProducts, inactiveProdcts),
                new DashboardResponse.DashboardCategory(activeCategories, inactiveCategories)));
    }

    public static record DashboardResponse(DashboardProduct product, DashboardCategory category) {
        public static record DashboardProduct(Long active, Long inactive) {
        }

        public static record DashboardCategory(Long active, Long inactive) {
        }
    }

}
