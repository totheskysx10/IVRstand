package com.good.ivrstand.app.service;


import com.good.ivrstand.app.repository.AdditionRepository;
import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.exception.FileDuplicateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с дополнениями
 */
@Component
@Slf4j
public class AdditionService {

    private final AdditionRepository additionRepository;
    private final SpeechService speechService;
    private final EncodeService encodeService;

    @Autowired
    public AdditionService(AdditionRepository additionRepository, SpeechService speechService, EncodeService encodeService) {
        this.additionRepository = additionRepository;
        this.speechService = speechService;
        this.encodeService = encodeService;
    }

    /**
     * Создает новое дополнение.
     *
     * @param addition Создаваемое дополнение.
     * @return Сохраненное дополнение.
     * @throws IllegalArgumentException Если переданное дополнение равна null.
     * @throws RuntimeException         Если возникла ошибка при создании дополнения.
     */
    public Addition createAddition(Addition addition, boolean enableAudio) {
        if (addition == null)
            throw new IllegalArgumentException("Дополнение не может быть null");

        try {
            if (enableAudio) {
                generateDescriptionAudio(addition);
                String titleAudio = speechService.generateAudio(addition.getTitle());
                addition.setTitleAudio(titleAudio);
            }
            Addition savedAddition = additionRepository.save(addition);
            log.info("Создано дополнение с id {}", savedAddition.getId());
            return savedAddition;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании дополнения", e);
        }
    }

    /**
     * Получает дополнение по его идентификатору.
     *
     * @param additionId Идентификатор дополнения.
     * @return Найденное дополнение.
     * @throws IllegalArgumentException Если дополнение с указанным идентификатором не найдено.
     */
    public Addition getAdditionById(long additionId) {
        Addition foundAddition = additionRepository.findById(additionId);
        if (foundAddition == null) {
            throw new IllegalArgumentException("Дополнение с id " + additionId + " не найдено");
        } else {
            log.debug("Найдено дополнение с id {}", additionId);
            return foundAddition;
        }
    }

    /**
     * Удаляет доплнение по его идентификатору.
     *
     * @param additionId Идентификатор дополнения.
     * @throws IllegalArgumentException Если дополнение с указанным идентификатором не найдено.
     */
    public void deleteAddition(long additionId) {
        additionRepository.deleteById(additionId);
        log.info("Удалено дополнение с id {}", additionId);
    }

    /**
     * Обновляет заголовок дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param title      Новый заголовок дополнения.
     */
    public void updateTitleToAddition(long additionId, String title) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setTitle(title);
            additionRepository.save(addition);
            log.info("Заголовок обновлен для дополнения с id {}", additionId);
        }
    }

    /**
     * Обновляет описание дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param desc       Новое описание дополнения.
     */
    public void updateDescriptionToAddition(long additionId, String desc, boolean enableAudio) throws IOException, FileDuplicateException {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setDescription(desc);
            addition.setDescriptionHash(encodeService.generateHashForAudio(desc));
            if (enableAudio) {
                generateDescriptionAudio(addition);
            } else {
                addition.getAudio().clear();
            }
            additionRepository.save(addition);
            log.info("Описание обновлено для дополнения с id {}", additionId);
        }
    }

    /**
     * Обновляет ссылку на GIF-анимацию дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param gifLink    Новая ссылка на GIF дополнения.
     */
    public void updateGifLinkToAddition(long additionId, String gifLink) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setGifLink(gifLink);
            additionRepository.save(addition);
            log.info("Ссылка на GIF обновлена для дополнения с id {}", additionId);
        }
    }

    /**
     * Обновляет ссылку на GIF-превью дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param gifPreview Новая ссылка на GIF превью.
     */
    public void updateGifPreviewToAddition(long additionId, String gifPreview) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setGifPreview(gifPreview);
            additionRepository.save(addition);
            log.info("Ссылка на GIF-превью обновлена для дополнения с id {}", additionId);
        }
    }

    /**
     * Обновляет ссылку на главную иконку дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param icon       Новая ссылка на главную иконку.
     */
    public void updateMainIconToAddition(long additionId, String icon) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setMainIconLink(icon);
            additionRepository.save(addition);
            log.info("Ссылка на главную иконку обновлена для дополнения с id {}", additionId);
        }
    }

    /**
     * Ищет дополнения по идентификатору категории, с поддержкой пагинации.
     *
     * @param itemId   Идентификатор услуги.
     * @param pageable Настройки пагинации.
     * @return Страница найденных дополнений.
     */
    public Page<Addition> findByItemId(long itemId, Pageable pageable) {
        return additionRepository.findByItemId(itemId, pageable);
    }

    /**
     * Добавляет иконку для дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param iconLink   Иконка.
     */
    public void addIcon(long additionId, String iconLink) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            if (!addition.getIconLinks().contains(iconLink)) {
                addition.getIconLinks().add(iconLink);
                additionRepository.save(addition);
                log.info("Добавлена иконка для дополнения с id {}", additionId);
            } else
                log.warn("Иконка для дополнения с id {} уже была добавлена раннее!", additionId);
        }
    }

    /**
     * Удаляет иконку у дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param iconLink   Иконка.
     */
    public void removeIcon(long additionId, String iconLink) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            if (addition.getIconLinks().contains(iconLink)) {
                addition.getIconLinks().remove(iconLink);
                additionRepository.save(addition);
                log.info("Удалена иконка для дополнения с id {}", additionId);
            }
        }
    }

    /**
     * Чистит иконки у дополнения.
     *
     * @param additionId Идентификатор дополнения.
     */
    public void clearIcons(long additionId) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.getIconLinks().clear();
            additionRepository.save(addition);
            log.info("Очищены иконки у дополнения {}", additionId);
        }
    }

    /**
     * Генерирует аудио заголовка дополнения.
     *
     * @param additionId Идентификатор дополнения.
     */
    public void generateTitleAudio(long additionId) throws IOException, FileDuplicateException {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            if (addition.getTitleAudio() == null) {
                String titleAudio = speechService.generateAudio(addition.getTitle());
                addition.setTitleAudio(titleAudio);
                additionRepository.save(addition);
                log.info("Сгенерировано аудио заголовка для дополнения с id {}", additionId);
            } else
                log.warn("У дополнения {} уже есть аудио заголовка!", additionId);
        }
    }

    /**
     * Удаляет аудио заголовка дополнения.
     *
     * @param additionId Идентификатор дополнения.
     */
    public void removeTitleAudio(long additionId) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            if (addition.getTitleAudio() == null) {
                log.warn("У дополнения {} нет аудио заголовка!", additionId);
            } else {
                addition.setTitleAudio(null);
                additionRepository.save(addition);
                log.info("У дополнения {} удалено аудио заголовка", additionId);
            }
        }
    }

    /**
     * Генерирует аудио для описания дополнения.
     * По хэш-функции проверяет, не было ли раннее сгенерировано такое аудио.
     *
     * @param addition дополнение
     * @throws IOException исключение
     */
    private void generateDescriptionAudio(Addition addition) throws IOException, FileDuplicateException {
        Page<Addition> additionsWithSameDescriptionRequest = additionRepository.findByHashAndAudioExistence(addition.getDescriptionHash(), PageRequest.of(0, 1));
        if (additionsWithSameDescriptionRequest.hasContent()) {
            Addition additionWithSameDescription = additionsWithSameDescriptionRequest.getContent().get(0);
            List<String> sameAudio = new ArrayList<>(additionWithSameDescription.getAudio());
            addition.getAudio().clear();
            for (String audioLink : sameAudio) {
                addition.getAudio().add(audioLink);
            }
        } else {
            String[] descriptionBlocks = speechService.splitDescription(addition.getDescription());
            for (String block : descriptionBlocks) {
                String audioLink = speechService.generateAudio(block);
                addition.getAudio().add(audioLink);
            }
        }
    }
}
