package com.menuonline.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Service
public class SimpleStorageBucketSerivce {

    private final S3Client s3Client;

    private final BucketConfig bucketConfig;

    public SimpleStorageBucketSerivce(BucketConfig bucketConfig) {
        log.info("constructor - bucketConfig: {}", bucketConfig);
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

    public void upload(Long userId, Long productId, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketConfig.bucketName())
                .key(getBucketKey(userId, productId))
                .build();
        RequestBody fromInputStream = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

        PutObjectResponse putObject = s3Client.putObject(putObjectRequest, fromInputStream);
        log.info("upload image: success:{}", putObject.sdkHttpResponse().isSuccessful());
    }

    public byte[] getImage(Long userId, Long productId) throws IOException {
        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(req -> {
                req.bucket(bucketConfig.bucketName())
                        .key(getBucketKey(userId, productId));
            });
            return object.readAllBytes();
        } catch (NoSuchKeyException e) {
            return null;
        }
    }

    private String getBucketKey(Long userId, Long productId) {
        return "user_" + userId + "/" + "product_" + productId;
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
}
