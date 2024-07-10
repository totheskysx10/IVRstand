package com.good.ivrstand.extern.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
public class S3Config {

    @Value("${yandex.cloud.access}")
    private String accessKey;

    @Value("${yandex.cloud.secret}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        log.info("Building S3 client");
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of("ru-central1"))
                .endpointOverride(URI.create("https://storage.yandexcloud.net"))
                .build();
    }
}
