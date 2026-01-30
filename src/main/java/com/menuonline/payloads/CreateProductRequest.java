package com.menuonline.payloads;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        String name,
        String description,
        List<PriceRequest> prices,
        long categoryId,
        boolean active) {

    public static record PriceRequest(
            BigDecimal value,
            String unit) {
    }
}
