package com.good.ivrstand.app;


import com.good.ivrstand.domain.Addition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdditionService {

    private final AdditionRepository additionRepository;

    @Autowired
    public AdditionService(AdditionRepository additionRepository) {
        this.additionRepository = additionRepository;
    }

    /**
     * Создает новое дополнение.
     *
     * @param addition Создаваемое дополнение.
     * @return Сохраненное дополнение.
     * @throws IllegalArgumentException Если переданное дополнение равна null.
     * @throws RuntimeException         Если возникла ошибка при создании дополнения.
     */
    public Addition createAddition(Addition addition) {
        if (addition == null)
            throw new IllegalArgumentException("Дополнение не может быть null");

        try {
            Addition savedAddition = additionRepository.save(addition);
            log.info("Создано дополнение с id {}", savedAddition.getId());
            return savedAddition;
        }
        catch (Exception e) {
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
        }
        else {
            log.info("Найдено дополнение с id {}", additionId);
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
     * @param title   Новый заголовок дополнения.
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
     * @param desc   Новое описание дополнения.
     */
    public void updateDescriptionToAddition(long additionId, String desc) {
        Addition addition = getAdditionById(additionId);
        if (addition != null) {
            addition.setDescription(desc);
            additionRepository.save(addition);
            log.info("Описание обновлено для дополнения с id {}", additionId);
        }
    }

    /**
     * Обновляет ссылку на GIF-анимацию дополнения.
     *
     * @param additionId Идентификатор дополнения.
     * @param gifLink   Новая ссылка на GIF дополнения.
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
     * Ищет дополнения по идентификатору категории, с поддержкой пагинации.
     *
     * @param itemId    Идентификатор услуги.
     * @param pageable Настройки пагинации.
     * @return Страница найденных дополнений.
     */
    public Page<Addition> findByItemId(long itemId, Pageable pageable) {
        return additionRepository.findByItemId(itemId, pageable);
    }
}
