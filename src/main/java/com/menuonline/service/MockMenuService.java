package com.menuonline.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.entity.Category;
import com.menuonline.entity.Price;
import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.PriceRepository;
import com.menuonline.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockMenuService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final PriceRepository priceRepository;

    @Transactional
    public void create(UserEntity user) {
        Category burgers = categoryRepository.save(getCategory(user, "Burguers"));
        Category pizzas = categoryRepository.save(getCategory(user, "Pizzas"));

        String classicoDescription = """
                Hambúrguer artesanal de 90gr, prensado na chapa, gerando uma crostinha surpreendente e deliciosa de sabor irresistível, pão de smash tostado na manteiga, queijo e 1 molho a escolha.
                """;
        Product classico = productRepository.save(getProduct(burgers, "Classico", classicoDescription));

        String costelaDescription = """
                Costela bovina assada lentamente, desfiada, pão tipo baguete tostado na manteiga, queijo e molho vinagrete especial.
                """;
        Product costela = productRepository.save(getProduct(burgers, "Costela", costelaDescription));

        String margheritaDescription = """
                Molho de tomates frescos, manjericão, parmesão, mozzarella e rodelas de tomates.
                """;
        Product margherita = productRepository.save(getProduct(pizzas, "Margherita", margheritaDescription));

        priceRepository.save(getPrice(classico, null, 24.90));

        priceRepository.save(getPrice(costela, null, 32.90));

        priceRepository.saveAll(List.of(getPrice(margherita, "Inteira", 59.90), getPrice(margherita, "Brotinho", 39.90)));

    }

    private Product getProduct(Category c, String name, String description) {
        Product product = new Product();
        product.setName(name);
        product.setActive(true);
        product.setDescription(description);
        product.setCategory(c);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private Price getPrice(Product product, String unit, double value) {
        Price price = new Price();
        price.setProduct(product);
        price.setUnit(unit);
        price.setValue(BigDecimal.valueOf(value));
        return price;
    }

    private Category getCategory(UserEntity user, String name) {
        Category category = new Category();
        category.setEnabled(true);
        category.setName(name);
        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

}
