package com.menuonline.payloads;

import java.math.BigDecimal;
import java.util.List;

import com.menuonline.entity.Product;
import com.menuonline.repository.ProductRepository.ProductProjection;

public record ProductResponse(
        long id,
        String name,
        String description,
        Boolean active,
        List<PriceResponse> prices,
        long categoryId,
        String categoryName) {

    public static ProductResponse from(ProductProjection product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.isActive(),
                List.of(),
                product.getCategoryId(),
                product.getCategoryName());
    }

    public static ProductResponse from(Product product) {
        List<PriceResponse> prices = product.getPrices().stream()
                .map(p -> new PriceResponse(p.getId(), p.getValue(), p.getUnit()))
                .toList();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.isActive(),
                prices,
                product.getCategory().getId(),
                product.getCategory().getName());
    }

    public static record PriceResponse(
            Long id,
            BigDecimal value,
            String unit) {
    }
}
