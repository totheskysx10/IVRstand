package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.S3Service;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.NoSuchFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile multipartFile, @RequestParam("folder") String folder) throws IOException {
        Map<String, String> response = new HashMap<>();
        try {
            String link = s3Service.uploadFile(multipartFile, folder);
            response.put("link", link);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (FileDuplicateException ex) {
            String link = s3Service.getLinkByFile(multipartFile, folder);
            response.put("link", link);
            response.put("message", "File duplicate.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        catch (IOException e) {
            response.put("message", "Failed to upload file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("link") String link) {
        try {
            String linkWithoutSpaces = link.replaceAll(" ", "%20");
            s3Service.deleteFileByUrl(linkWithoutSpaces);
            return ResponseEntity.ok().build();
        } catch (NoSuchFileException ex) {
            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
