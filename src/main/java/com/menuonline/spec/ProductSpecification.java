package com.menuonline.spec;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.menuonline.entity.Category;
import com.menuonline.entity.Product;

public record ProductSpecification() {

    public static Specification<Product> isCategory(Category category) {
        return (root, query, builder) -> {
            return root.get("category").equalTo(category);
        };
    }

    public static Specification<Product> inCategoryList(List<Category> ids) {
        return (root, query, builder) -> {
            return root.get("category").in(ids);
        };
    }

    public static Specification<Product> byName(String name) {
        return (root, query, builder) -> {
            return builder.like(
                    builder.lower(root.get("name")),
                    builder.lower(builder.literal("%" + name + "%")));
        };
    }

    public static Specification<Product> byStatus(boolean active) {
        return (root, query, builder) -> {
            if (active) {
                return builder.isTrue(root.get("active"));
            }
            return builder.isFalse(root.get("active"));
        };
    }
}
