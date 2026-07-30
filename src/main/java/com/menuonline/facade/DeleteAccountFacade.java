package com.menuonline.facade;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.entity.Category;
import com.menuonline.entity.Customization;
import com.menuonline.entity.Product;
import com.menuonline.entity.Subscription;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.repository.CategoryRepository;
import com.menuonline.repository.CustomizationRepository;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.SubscriptionRepository;
import com.menuonline.repository.UserRepository;
import com.menuonline.service.SimpleStorageBucketSerivce;
import com.menuonline.service.SubscriptionService;
import com.menuonline.utils.CryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteAccountFacade {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CustomizationRepository customizationRepository;
    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final SimpleStorageBucketSerivce bucketSerivce;

    @Transactional
    public void delete(UserEntity user, String password) throws IOException {
        log.info("delete - user:{}", user);
        if (CryptoUtil.validate(user.getPassword(), password)) {
            throw new HttpServiceException("Invalid password to delete account!");
        }

        Subscription currentSubs = subscriptionRepository.findCurrent(user.getId());

        if (!currentSubs.getFreeTier() && currentSubs.getStatus().equals(Subscription.Status.ACTIVE)) {
            throw new HttpServiceException("Active subscription, not able to delete account",
                    ErrorMessages.SUBSCRIPTION_STILL_ACTIVE_TO_DELETE_ACCOUNT, HttpStatus.BAD_REQUEST);
        }

        List<Category> categories = categoryRepository.findByUserIdOrderBySequence(user.getId());

        List<Product> products = productRepository
                .findByCategoryIdIn(categories.stream().map(Category::getId).toList());

        List<Customization> customizations = customizationRepository.findAllByUserId(user.getId());

        categoryRepository.deleteAll(categories);
        customizationRepository.deleteAll(customizations);

        bucketSerivce.deleteAll(products);

        userRepository.delete(user);
        log.info("delete account successfully - user:{}", user);
    }

}
