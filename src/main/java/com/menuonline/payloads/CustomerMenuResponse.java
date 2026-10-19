package com.menuonline.payloads;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.menuonline.entity.Customization;
import com.menuonline.entity.Schedule;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.ProductRepository.ProductMenuProjection;

public record CustomerMenuResponse(
        EstablishmentInfoResponse info,
        CustomizationResponse customization,
        List<ScheduleResponse> schedules,
        List<CategoryResponse> categories) {

    public static CustomerMenuResponse from(UserEntity user, List<Schedule> schedules,
            List<ProductMenuProjection> projection,
            Customization customization) {
        EstablishmentInfoResponse info = new EstablishmentInfoResponse(user.getId(),
                user.getEstablishmentName(), user.getEstablishmentDescription(), user.getInstagram(),
                user.getFacebook(), user.getWebsite(), user.getWhatsapp(), user.getWifiName(), user.getWifiPassword(),
                user.getPhone(), user.getImage(), user.getAddressLine(), user.getCode(), user.getCity());

        List<ScheduleResponse> scheduleResponses = ScheduleResponse.from(schedules);

        List<CategoryResponse> categories = new ArrayList<>();

        for (ProductMenuProjection proj : projection) {
            Optional<CategoryResponse> category = categories.stream()
                    .filter(cat -> cat.id() == proj.getCategoryId())
                    .findFirst();

            category.ifPresentOrElse(cat -> {
                cat.products()
                        .stream()
                        .filter(p -> p.id() == proj.getId())
                        .findFirst()
                        .ifPresentOrElse(p -> {
                            // PRICES
                            p.prices().stream()
                                    .filter(pr -> pr.id() == proj.getPriceId().longValue())
                                    .findFirst()
                                    .ifPresentOrElse(price -> {
                                    }, () -> p.prices().add(createPrice(proj)));

                            // TAGS
                            if (proj.getTagName() != null) {
                                p.tags().stream()
                                        .filter(t -> t.id().equals(proj.getTagId()))
                                        .findFirst()
                                        .orElseGet(() -> {
                                            p.tags().add(new TagsResponse(proj.getTagId(), proj.getTagName()));
                                            return null;
                                        });
                            }
                        }, () -> {
                            List<PriceResponse> prices = new ArrayList<>();
                            PriceResponse price = createPrice(proj);
                            prices.add(price);
                            cat.products().add(createProduct(proj, prices));
                        });
            }, () -> { // if category does not exist yet
                List<PriceResponse> prices = new ArrayList<>();
                PriceResponse price = createPrice(proj);
                prices.add(price);
                List<ProductResponse> products = new ArrayList<>();
                products.add(createProduct(proj, prices));
                CategoryResponse cat = new CategoryResponse(proj.getCategoryId(),
                        proj.getCategoryName(), products);
                categories.add(cat);
            });
        }
        return new CustomerMenuResponse(info, CustomizationResponse.from(customization), scheduleResponses, categories);
    }

    private static PriceResponse createPrice(ProductMenuProjection proj) {
        PriceResponse price = new PriceResponse(proj.getPriceId(), proj.getValue(), proj.getUnit());
        return price;
    }

    private static ProductResponse createProduct(ProductMenuProjection proj, List<PriceResponse> prices) {
        List<TagsResponse> tags = new ArrayList<>();
        if (proj.getTagName() != null) {
            tags.add(new TagsResponse(proj.getTagId(), proj.getTagName()));
        }
        ProductResponse product = new ProductResponse(proj.getId(),
                proj.getProductName(),
                proj.getImage(),
                proj.getDescription(),
                tags,
                prices);
        return product;
    }

    public static record CategoryResponse(Long id, String name, List<ProductResponse> products) {
    }

    public static record EstablishmentInfoResponse(
            Long id,
            String establishmentName,
            String description,
            String instagram,
            String facebook,
            String website,
            String whatsapp,
            String wifiName,
            String wifiPassword,
            String phone,
            String image,
            String addressLine,
            String addressCode,
            String city) {
    }

    public static record ProductResponse(
            Long id,
            String name,
            String image,
            String description,
            List<TagsResponse> tags,
            List<PriceResponse> prices) {
    }

    public static record TagsResponse(Long id, String name) {
    }

    public static record PriceResponse(Long id, BigDecimal value, String unit) {
    }
}
