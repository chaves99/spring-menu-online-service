package com.menuonline.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.menuonline.entity.Product;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.repository.ProductRepository;
import com.menuonline.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Service
public class SimpleStorageBucketSerivce {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final S3Client s3Client;

    private final BucketConfig bucketConfig;

    public SimpleStorageBucketSerivce(UserRepository userRepository,
            ProductRepository productRepository,
            BucketConfig bucketConfig) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.bucketConfig = bucketConfig;

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials
                .create(bucketConfig.accessKeyId(), bucketConfig.secretAccessKey());
        this.s3Client = S3Client
                .builder()
                .region(Region.of(bucketConfig.region()))
                .endpointOverride(URI.create(bucketConfig.url()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }

    public void uploadProduct(Long userId, Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new HttpServiceException(ErrorMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (product.getImage() != null) {
            this.delete(product.getImage());
        }

        String bucketKey = buildProductKey(userId, productId);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketConfig.bucketName())
                .key(bucketKey)
                .build();
        RequestBody fromInputStream = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

        PutObjectResponse putObject = s3Client.putObject(putObjectRequest, fromInputStream);

        log.info("upload image: success:{}", putObject.sdkHttpResponse().isSuccessful());

        product.setImage(this.getImageUrl(bucketKey));
        productRepository.save(product);

    }

    public void delete(Long userId, Long productId) throws IOException {
        this.delete(buildProductKey(userId, productId));
    }

    public void delete(String keyName) throws IOException {
        try {
            s3Client.deleteObject(req -> {
                req.bucket(bucketConfig.bucketName());
                req.key(keyName);
            });
        } catch (NoSuchKeyException ignored) {
        }
    }

    private String buildProductKey(Long userId, Long productId) {
        return "user_" + userId + "/" + "product_" + productId + "/" + UUID.randomUUID().toString();
    }

    private String getImageUrl(String key) {
        return "https://itimenu-product-images.fly.storage.tigris.dev/" + key;
    }

    public List<String> getAllBuckets() {
        List<String> list = new ArrayList<>();
        s3Client.listBucketsPaginator().forEach(b -> {
            b.buckets().forEach(bu -> {
                list.add(bu.name());
            });
        });
        return list;
    }

    public List<String> getObjectOnBucket() {
        List<String> list = new ArrayList<>();
        s3Client.listObjectsV2(req -> {
            req.bucket(bucketConfig.bucketName());
        }).contents().forEach(c -> {
            list.add(c.key());
        });
        return list;
    }

    @ConfigurationProperties(prefix = "s3")
    public static record BucketConfig(
            String url,
            String region,
            String bucketName,
            String accessKeyId,
            String secretAccessKey) {

        @ConstructorBinding
        public BucketConfig {
        }
    }

    public String uploadEstablishment(UserEntity user, MultipartFile file) throws IOException {
        if (user.getImage() != null) {
            this.delete(user.getImage());
        }
        String key = "user_" + user.getId() + "/establishment/" + UUID.randomUUID().toString();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketConfig.bucketName())
                .key(key)
                .build();
        RequestBody fromInputStream = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
        s3Client.putObject(putObjectRequest, fromInputStream);

        String imageUrl = getImageUrl(key);
        user.setImage(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

}
