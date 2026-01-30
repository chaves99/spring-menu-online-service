package com.menuonline.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.controller.ProductController.ProductFilterParamRequest;
import com.menuonline.entity.Category;
import com.menuonline.entity.Price;
import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.CreateProductRequest;
import com.menuonline.payloads.ProductResponse;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.PriceRepository;
import com.menuonline.repository.ProductRepository;
import com.menuonline.spec.ProductSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceRepository priceRepository;

    public Page<ProductResponse> getAll(UserEntity user, int page, int size,
            ProductFilterParamRequest filter) {
        log.info("get all products - user:{} page:{} size:{}", user.getId(), page, size);

        List<Specification<Product>> specs = new ArrayList<>();
        if (filter.categoryId() != null) {
            Category category = categoryRepository
                    .findByUserIdAndId(user.getId(), filter.categoryId())
                    .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
            specs.add(ProductSpecification.isCategory(category));
        } else {
            specs.add(ProductSpecification.inCategoryList(categoryRepository.findByUserId(user.getId())));
        }
        if (filter.name() != null) {
            specs.add(ProductSpecification.byName(filter.name()));
        }
        if (filter.active() != null) {
            specs.add(ProductSpecification.byStatus(filter.active()));
        }
        return productRepository.findAll(Specification.allOf(specs), PageRequest.of(page, size))
                .map(ProductResponse::from);
    }

    @Transactional
    public Product create(CreateProductRequest request) {
        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.CONFLICT));

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setActive(request.active());
        Product saved = productRepository.save(product);
        log.info("create product - id:{}", saved.getId());

        savePrices(request, product);

        return saved;
    }

    private void savePrices(CreateProductRequest request, Product product) {
        List<Price> prices = request.prices()
                .stream()
                .map(p -> new Price(null, p.value(), p.unit(), product))
                .toList();
        priceRepository.saveAll(prices);
    }

    public Optional<Product> getById(Long id, Long userId) {
        return productRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public void update(Long idProduct, CreateProductRequest request) {
        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.CONFLICT));

        Product product = productRepository
                .findById(idProduct)
                .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        priceRepository.deleteByProductId(idProduct);
        savePrices(request, product);

        product.setName(request.name());
        product.setActive(request.active());
        product.setDescription(request.description());
        product.setCategory(category);

        productRepository.saveAndFlush(product);
    }

    public void toggleActive(Long id, UserEntity user) {
        log.info("toggleActive - id:{}", id);
        Product product = productRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
        productRepository.toggleActive(product.getId());
    }

    public void delete(Long id, UserEntity user) {
        log.info("delete - id:{}", id);
        Product product = productRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
        productRepository.delete(product);
    }
}
