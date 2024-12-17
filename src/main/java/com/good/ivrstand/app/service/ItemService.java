package com.good.ivrstand.app.service;

import com.good.ivrstand.app.repository.ItemRepository;
import com.good.ivrstand.app.service.externinterfaces.FlaskApiVectorSearchService;
import com.good.ivrstand.app.service.externinterfaces.QdrantService;
import com.good.ivrstand.domain.*;
import com.good.ivrstand.exception.*;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import com.good.ivrstand.exception.notfound.ItemNotFoundException;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Сервис для работы с услугами
 */
@Component
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final FlaskApiVectorSearchService flaskApiVectorSearchService;
    private final AdditionService additionService;
    private final QdrantService qdrantService;
    private final SpeechService speechService;
    private final EncodeService encodeService;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       CategoryService categoryService,
                       FlaskApiVectorSearchService flaskApiVectorSearchService,
                       AdditionService additionService,
                       QdrantService qdrantService,
                       SpeechService speechService,
                       EncodeService encodeService) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
        this.additionService = additionService;
        this.qdrantService = qdrantService;
        this.speechService = speechService;
        this.encodeService = encodeService;
    }

    /**
     * Создает новую услугу.
     *
     * @param item Создаваемая услуга.
     * @return Сохраненная услуга.
     * @throws IllegalArgumentException Если переданная услуга равна null.
     * @throws RuntimeException         Если возникла ошибка при создании услуги.
     */
    public Item createItem(Item item, boolean enableAudio) {
        if (item == null) {
            throw new IllegalArgumentException("Услуга не может быть null");
        }

        try {
            if (enableAudio) {
                generateDescriptionAudio(item);
                String titleAudio = speechService.generateAudio(item.getTitle());
                item.setTitleAudio(titleAudio);
            }
            Item savedItem = itemRepository.save(item);
            AddTitleRequest addTitleRequest = new AddTitleRequest(formatTitle(savedItem), savedItem.getId());
            flaskApiVectorSearchService.addTitle(addTitleRequest);
            log.info("Создана услуга с id {}", savedItem.getId());
            return savedItem;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании услуги", e);
        }
    }

    /**
     * Получает услугу по ее идентификатору.
     *
     * @param itemId Идентификатор услуги.
     * @return Найденная услуга.
     * @throws ItemNotFoundException Если услуга с указанным идентификатором не найдена.
     */
    public Item getItemById(long itemId) throws ItemNotFoundException {
        Item foundItem = itemRepository.findById(itemId);
        if (foundItem == null) {
            throw new ItemNotFoundException("Услуга с id " + itemId + " не найдена");
        } else {
            log.debug("Найдена услуга с id {}", itemId);
            return foundItem;
        }
    }

    /**
     * Удаляет услугу по ее идентификатору.
     * Если к услуге привязаны дополнения, удаляет и их.
     *
     * @param itemId Идентификатор услуги.
     */
    public void deleteItem(long itemId) throws ItemNotFoundException {
        Item foundItem = getItemById(itemId);

        if (!foundItem.getAdditions().isEmpty())
            foundItem.getAdditions().stream()
                    .map(Addition::getId)
                    .forEach(additionService::deleteAddition);
        itemRepository.deleteById(itemId);
        deleteQdrantTitle(foundItem);
        log.info("Удалена услуга с id {}", itemId);
    }

    /**
     * Добавляет услугу в категорию.
     *
     * @param itemId     Идентификатор услуги.
     * @param categoryId Идентификатор категории.
     * @throws ItemCategoryAddDeleteException если услуга уже в другой категории или категория не конечная
     */
    public void addToCategory(long itemId, long categoryId) throws ItemCategoryAddDeleteException, ItemNotFoundException, CategoryNotFoundException {
        Item item = getItemById(itemId);
        Category category = categoryService.getCategoryById(categoryId);

        if (category.getChildrenCategories().isEmpty()) {
            if (item.getCategory() == null) {
                deleteQdrantTitle(item);
                item.setCategory(category);
                itemRepository.save(item);
                addQdrantTitle(item);
                log.info("Услуга с id {} добавлена в категорию с id {}", itemId, categoryId);
            } else
                throw new ItemCategoryAddDeleteException(String.format("Услуга с id %s уже в другой категории!", itemId));
        } else
            throw new ItemCategoryAddDeleteException(String.format("В категории с id %s есть подкатегории - услугу можно добавить только в конечную подкатегорию!", categoryId));
    }

    /**
     * Удаляет услугу из категории.
     *
     * @param itemId Идентификатор услуги.
     * @throws ItemCategoryAddDeleteException если услуга ни в одной категории не находится
     */
    public void removeFromCategory(long itemId) throws ItemCategoryAddDeleteException, ItemNotFoundException {
        Item item = getItemById(itemId);

        if (item.getCategory() != null) {
            deleteQdrantTitle(item);
            item.setCategory(null);
            itemRepository.save(item);
            addQdrantTitle(item);
            log.info("Услуга с id {} удалена из категории", itemId);
        } else
            throw new ItemCategoryAddDeleteException(String.format("Услуга с id %s не относится ни к одной из категорий!", itemId));
    }

    /**
     * Получает все услуги из базы данных, с поддержкой пагинации.
     *
     * @param pageable Настройки пагинации.
     * @return Страница услуг.
     */
    public Page<Item> getAllItemsInBase(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    /**
     * Ищет услуги по заголовку, с поддержкой пагинации.
     * Сравнивает количество услуг в базе и количество найденных.
     * Если в базе больше или равно 4, то должен возвращать 4.
     * Если меньше 4, то кол-во услуг в базе равно кол-ву найденных.
     * Если условия не выполнены, идёт запрос на синхронизацию БД
     *
     * @param title    Часть заголовка для поиска.
     * @param pageable Настройки пагинации.
     * @return страница найденных услуг
     */
    public Page<Item> findItemsByTitle(String title, Pageable pageable) throws ItemsFindException, ItemNotFoundException {
        List<Long> result = flaskApiVectorSearchService.getItemIds(title);

        List<Item> items = new ArrayList<>();
        for (Long element : result) {
            Item item = getItemById(element);
            if (item != null) {
                items.add(item);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());
        Page<Item> page = new PageImpl<>(items.subList(start, end), pageable, items.size());

        Pageable pageableCheckQuantity = PageRequest.of(0, 7);
        Page<Item> itemsPage = getAllItemsInBase(pageableCheckQuantity);
        int itemsInBaseQuantity = itemsPage.getContent().size();
        int itemsFoundQuantity = page.getContent().size();

        boolean shouldSync = itemsInBaseQuantity < 4
                ? itemsInBaseQuantity != itemsFoundQuantity
                : itemsFoundQuantity != 4;

        if (shouldSync) {
            qdrantService.syncDatabase();
        }

        return page;
    }

    /**
     * Ищет услуги без категории, с поддержкой пагинации.
     *
     * @param pageable Настройки пагинации.
     * @return Страница найденных услуг.
     */
    public Page<Item> findItemsWithoutCategory(Pageable pageable) {
        return itemRepository.findItemsWithNullCategory(pageable);
    }

    /**
     * Ищет услуги по категории с поддержкой пагинации.
     *
     * @param categoryId Категория для поиска.
     * @param pageable   Настройки пагинации.
     * @return Страница найденных услуг.
     */
    public Page<Item> findItemsByCategory(long categoryId, Pageable pageable) {
        return itemRepository.findByCategoryId(categoryId, pageable);
    }

    /**
     * Обновляет описание услуги.
     *
     * @param itemId Идентификатор услуги.
     * @param desc   Новое описание услуги.
     */
    public void updateDescriptionToItem(long itemId, String desc, boolean enableAudio) throws IOException, FileDuplicateException, ItemNotFoundException {
        Item item = getItemById(itemId);
        deleteQdrantTitle(item);
        item.setDescription(desc);
        item.setDescriptionHash(encodeService.generateHashForAudio(desc));
        if (enableAudio) {
            generateDescriptionAudio(item);
        } else {
            item.getAudio().clear();
        }
        itemRepository.save(item);
        addQdrantTitle(item);
        log.info("Описание обновлено для услуги с id {}", itemId);
    }

    /**
     * Обновляет ссылку на GIF-анимацию услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param gifLink Новая ссылка на GIF услуги.
     */
    public void updateGifLinkToItem(long itemId, String gifLink) throws ItemNotFoundException {
        Item item = getItemById(itemId);
        item.setGifLink(gifLink);
        itemRepository.save(item);
        log.info("Ссылка на GIF обновлена для услуги с id {}", itemId);
    }

    /**
     * Обновляет ссылку на главную иконку услуги.
     *
     * @param itemId Идентификатор услуги.
     * @param icon   Новая ссылка на главную иконку.
     */
    public void updateMainIconToItem(long itemId, String icon) throws ItemNotFoundException {
        Item item = getItemById(itemId);
        item.setMainIconLink(icon);
        itemRepository.save(item);
        log.info("Ссылка на главную иконку обновлена для услуги с id {}", itemId);
    }

    /**
     * Обновляет ссылку на GIF-превью услуги.
     *
     * @param itemId     Идентификатор услуги.
     * @param gifPreview Новая ссылка на GIF превью услуги.
     */
    public void updateGifPreviewToItem(long itemId, String gifPreview) throws ItemNotFoundException {
        Item item = getItemById(itemId);
        item.setGifPreview(gifPreview);
        itemRepository.save(item);
        log.info("Ссылка на GIF-превью обновлена для услуги с id {}", itemId);
    }

    /**
     * Добавляет иконку для услуги.
     *
     * @param itemId   Идентификатор услуги.
     * @param iconLink Иконка.
     * @throws ItemUpdateException если эта иконка уже есть у услуги
     */
    public void addIcon(long itemId, String iconLink) throws ItemUpdateException, ItemNotFoundException {
        Item item = getItemById(itemId);
        if (!item.getIconLinks().contains(iconLink)) {
            item.getIconLinks().add(iconLink);
            itemRepository.save(item);
            log.info("Добавлена иконка для услуги с id {}", itemId);
        } else
            throw new ItemUpdateException(String.format("Иконка для услуги с id %s уже была добавлена раннее!", itemId));
    }

    /**
     * Удаляет иконку у услуги.
     *
     * @param itemId   Идентификатор услуги.
     * @param iconLink Иконка.
     */
    public void removeIcon(long itemId, String iconLink) throws ItemNotFoundException {
        Item item = getItemById(itemId);
        if (item.getIconLinks().contains(iconLink)) {
            item.getIconLinks().remove(iconLink);
            itemRepository.save(item);
            log.info("Удалена иконка для услуги с id {}", itemId);
        }
    }

    /**
     * Чистит иконки у услуги.
     *
     * @param itemId Идентификатор услуги.
     */
    public void clearIcons(long itemId) throws ItemNotFoundException {
        Item item = getItemById(itemId);
        item.getIconLinks().clear();
        itemRepository.save(item);
        log.info("Очищены иконки у услуги {}", itemId);
    }

    /**
     * Добавляет ключевое слово для услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param keyword Слово.
     * @throws ItemUpdateException если это слово уже есть у услуги
     */
    public void addKeyword(long itemId, String keyword) throws ItemUpdateException, ItemNotFoundException {
        Item item = getItemById(itemId);
        if (!item.getKeywords().contains(keyword)) {
            deleteQdrantTitle(item);
            item.getKeywords().add(keyword);
            itemRepository.save(item);
            addQdrantTitle(item);
            log.info("Добавлено ключевое слово для услуги с id {}", itemId);
        } else
            throw new ItemUpdateException(String.format("Ключевое слово для услуги с id %s уже было добавлено раннее!", itemId));
    }

    /**
     * Удаляет ключевое слово у услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param keyword Слово.
     */
    public void removeKeyword(long itemId, String keyword) throws ItemNotFoundException {
        Item item = getItemById(itemId);

        if (item.getKeywords().contains(keyword)) {
            deleteQdrantTitle(item);
            item.getKeywords().remove(keyword);
            itemRepository.save(item);
            addQdrantTitle(item);
            log.info("Удалено ключевое слово для услуги с id {}", itemId);
        }
    }

    /**
     * Чистит ключевые слова у услуги.
     *
     * @param itemId Идентификатор услуги.
     */
    public void clearKeywords(long itemId) throws ItemNotFoundException {
        Item item = getItemById(itemId);

        deleteQdrantTitle(item);
        item.getKeywords().clear();
        itemRepository.save(item);
        addQdrantTitle(item);
        log.info("Очищены ключевые слова у услуги {}", itemId);
    }

    /**
     * Генерирует аудио заголовка услуги.
     *
     * @param itemId Идентификатор услуги.
     * @throws ItemUpdateException если аудио заголовка уже есть у услуги
     */
    public void generateTitleAudio(long itemId) throws IOException, FileDuplicateException, ItemUpdateException, ItemNotFoundException {
        Item item = getItemById(itemId);
        if (item.getTitleAudio() == null) {
            String titleAudio = speechService.generateAudio(item.getTitle());
            item.setTitleAudio(titleAudio);
            itemRepository.save(item);
            log.info("Сгенерировано аудио заголовка для услуги с id {}", itemId);
        } else
            throw new ItemUpdateException(String.format("У услуги %s уже есть аудио заголовка!", itemId));
    }

    /**
     * Удаляет аудио заголовка услуги.
     *
     * @param itemId Идентификатор услуги.
     * @throws ItemUpdateException если аудио заголовка уже нет у услуги
     */
    public void removeTitleAudio(long itemId) throws ItemUpdateException, ItemNotFoundException {
        Item item = getItemById(itemId);
        if (item.getTitleAudio() == null) {
            throw new ItemUpdateException(String.format("У услуги %s нет аудио заголовка!", itemId));
        } else {
            item.setTitleAudio(null);
            itemRepository.save(item);
            log.info("У услуги {} удалено аудио заголовка", itemId);
        }
    }

    /**
     * Удаляет услугу из базы Qdrant.
     *
     * @param item Услуга.
     */
    private void deleteQdrantTitle(Item item) {
        TitleRequest titleRequest = new TitleRequest(formatTitle(item));
        flaskApiVectorSearchService.deleteTitle(titleRequest);
    }

    /**
     * Добавляет услугу в базу Qdrant.
     *
     * @param item Услуга.
     */
    private void addQdrantTitle(Item item) {
        AddTitleRequest addTitleRequest = new AddTitleRequest(formatTitle(item), item.getId());
        flaskApiVectorSearchService.addTitle(addTitleRequest);
    }

    /**
     * Формирует строку для вектора услуги в формате
     * "заголовок ключевые_слова категория описание"
     * или "заголовок ключевые_слова описание", если нет категории.
     *
     * @param item Услуга.
     * @return Отформатированная строка.
     */
    private String formatTitle(Item item) {
        String keywords;
        if (item.getKeywords().isEmpty())
            keywords = "";
        else
            keywords = String.join(" ", item.getKeywords());

        String category = item.getCategory() != null ? item.getCategory().getTitle() : "";
        String description = item.getDescription();

        if (!category.isEmpty()) {
            return item.getTitle() + " " + keywords + " " + category + " " + description;
        } else {
            return item.getTitle() + " " + keywords + " " + description;
        }
    }

    /**
     * Генерирует аудио для описания услуги.
     * По хэш-функции проверяет, не было ли раннее сгенерировано такое аудио.
     *
     * @param item услуга
     */
    private void generateDescriptionAudio(Item item) throws IOException, FileDuplicateException {
        Page<Item> itemsWithSameDescriptionRequest = itemRepository.findByHashAndAudioExistence(item.getDescriptionHash(), PageRequest.of(0, 1));
        if (itemsWithSameDescriptionRequest.hasContent()) {
            Item itemWithSameDescription = itemsWithSameDescriptionRequest.getContent().get(0);
            List<String> sameAudio = new ArrayList<>(itemWithSameDescription.getAudio());
            item.getAudio().clear();
            for (String audioLink : sameAudio) {
                item.getAudio().add(audioLink);
            }
        } else {
            String[] descriptionBlocks = speechService.splitDescription(item.getDescription());
            for (String block : descriptionBlocks) {
                String audioLink = speechService.generateAudio(block);
                item.getAudio().add(audioLink);
            }
        }
    }
}
