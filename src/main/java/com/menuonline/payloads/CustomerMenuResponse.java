package com.menuonline.payloads;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.ProductRepository.ProductMenuProjection;

public record CustomerMenuResponse(
        EstablishmentInfoResponse info,
        List<ScheduleResponse> schedules,
        List<CategoryResponse> categories) {

    public static CustomerMenuResponse from(UserEntity user, List<Schedule> schedules,
            List<ProductMenuProjection> projection) {
        EstablishmentInfoResponse info = new EstablishmentInfoResponse(user.getId(),
                user.getEstablishmentName(), user.getInstagram(), user.getFacebook(),
                user.getWebsite(), user.getWhatsapp(), user.getPhone(), user.getAddressLine(),
                user.getCode(), user.getCity());

        List<ScheduleResponse> scheduleResponses = ScheduleResponse.from(schedules);

        List<CategoryResponse> categories = new ArrayList<>();

        projection.forEach(proj -> {
            categories.stream()
                    .filter(cat -> cat.id() == proj.getCategoryId())
                    .findFirst()
                    .ifPresentOrElse(
                            cat -> {
                                cat.products()
                                        .stream()
                                        .filter(p -> p.id() == proj.getId())
                                        .findFirst()
                                        .ifPresentOrElse(p -> {
                                            p.prices().stream()
                                                    .filter(pr -> pr.id() == proj.getPriceId())
                                                    .findFirst()
                                                    .ifPresentOrElse(price -> {
                                                    },
                                                            () -> p.prices().add(getPrice(proj)));
                                        }, () -> {
                                            List<PriceResponse> prices = new ArrayList<>();
                                            PriceResponse price = getPrice(proj);
                                            prices.add(price);
                                            cat.products().add(getProduct(proj, prices));
                                        });
                            },
                            () -> {
                                List<PriceResponse> prices = new ArrayList<>();
                                PriceResponse price = getPrice(proj);
                                prices.add(price);
                                List<ProductResponse> products = new ArrayList<>();
                                products.add(getProduct(proj, prices));
                                CategoryResponse cat = new CategoryResponse(proj.getCategoryId(),
                                        proj.getCategoryName(), products);
                                categories.add(cat);
                            });
        });
        return new CustomerMenuResponse(info, scheduleResponses, categories);
    }

    private static PriceResponse getPrice(ProductMenuProjection proj) {
        PriceResponse price = new PriceResponse(proj.getPriceId(), proj.getValue(), proj.getUnit());
        return price;
    }

    private static ProductResponse getProduct(ProductMenuProjection proj, List<PriceResponse> prices) {
        ProductResponse product = new ProductResponse(proj.getId(),
                proj.getProductName(),
                proj.getDescription(),
                prices);
        return product;
    }

    public static record CategoryResponse(Long id, String name, List<ProductResponse> products) {
    }

    public static record EstablishmentInfoResponse(
            Long id,
            String establishmentName,
            String instagram,
            String facebook,
            String website,
            String whatsapp,
            String phone,
            String addressLine,
            String addressCode,
            String city) {
    }

    public static record ProductResponse(
            Long id,
            String name,
            String description,
            List<PriceResponse> prices) {
    }

    public static record PriceResponse(Long id, BigDecimal value, String unit) {
    }
}
