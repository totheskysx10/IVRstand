package com.good.ivrstand.app.service.externinterfaces;

import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.NoSuchFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Сервис для работы с хранилищем S3.
 */
public interface S3Service {

    /**
     * Загружает файл в S3 и возвращает ссылку на него.
     *
     * @param multipartFile файл для загрузки
     * @param folderName    имя папки, в которую будет загружен файл
     * @return ссылка на загруженный файл
     * @throws IOException            если происходит ошибка ввода/вывода
     * @throws FileDuplicateException если файл с таким именем уже существует
     */
    String uploadFile(MultipartFile multipartFile, String folderName) throws IOException, FileDuplicateException;

    /**
     * Удаляет файл из S3 по указанному URL.
     *
     * @param url ссылка на файл, который нужно удалить
     * @throws NoSuchFileException если файл не найден
     */
    void deleteFileByUrl(String url) throws NoSuchFileException;

    /**
     * Возвращает ссылку на файл в S3.
     *
     * @param multipartFile файл, для которого нужно получить ссылку
     * @param folderName    имя папки, в которой находится файл
     * @return ссылка на файл
     */
    String getLinkByFile(MultipartFile multipartFile, String folderName);
}
