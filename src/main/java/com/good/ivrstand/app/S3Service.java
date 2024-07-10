package com.good.ivrstand.app;

import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.NoSuchFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Component
public class S3Service {

    private final S3Client s3Client;

    @Value("${yandex.cloud.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public String uploadFile(MultipartFile multipartFile, String folderName) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        int lastDot = fileName.lastIndexOf('.');
        String name = fileName.substring(0, lastDot);
        String extension = fileName.substring(lastDot);

        String key = folderName + "/" + name + "_" + generateUUID() + extension;

        if (!doesFileExist(key)) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            log.info("Файл добавлен в S3.");

            return String.format("https://storage.yandexcloud.net/%s/%s", bucketName, key);
        }
        else {
            throw new FileDuplicateException("Файл с таким именем уже был добавлен!");
        }
    }

    public void deleteFileByUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath().substring(1);
        String key = path.substring(path.indexOf(bucketName) + bucketName.length() + 1);
        if (doesFileExist(key)) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Файл удалён из S3.");
        } else {
            throw new NoSuchFileException("Файл не найден!");
        }
    }

    public boolean doesFileExist(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);

            return true;
        }
        catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public String getLinkByFile(MultipartFile multipartFile, String folderName) {
        String key = folderName + "/" + multipartFile.getOriginalFilename();
        return String.format("https://storage.yandexcloud.net/%s/%s", bucketName, key);
    }
}
