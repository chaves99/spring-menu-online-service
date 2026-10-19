package com.menuonline.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.entity.Product;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.TagsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagsService {

    private final TagsRepository tagsRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void delete(Long tagId) {
        tagsRepository.findById(tagId).ifPresent(tag -> {
            List<Product> products = productRepository.findProductByTags_Id(tagId);
            for (Product product : products) {
                product.getTags().remove(tag);
            }
            tagsRepository.delete(tag);
        });
    }
}
