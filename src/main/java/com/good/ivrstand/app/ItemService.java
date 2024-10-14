package com.good.ivrstand.app;

import com.good.ivrstand.domain.*;
import com.good.ivrstand.exception.ItemsFindException;
import com.good.ivrstand.extern.api.requests.AddTitleRequest;
import com.good.ivrstand.extern.api.requests.TitleRequest;
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
 * Сервисный класс для работы с услугами (Items).
 * Обеспечивает операции создания, получения, обновления и удаления услуг,
 * а также поиск услуг по различным критериям.
 */
@Component
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final FlaskApiVectorSearchService flaskApiVectorSearchService;
    private final AdditionService additionService;
    private final QdrantService qdrantService;
    private final DescriptionService descriptionService;
    private final SpeechService speechService;
    private final EncodeService encodeService;

    @Autowired
    public ItemService(ItemRepository itemRepository, CategoryService categoryService, FlaskApiVectorSearchService flaskApiVectorSearchService, AdditionService additionService, QdrantService qdrantService, DescriptionService descriptionService, SpeechService speechService, EncodeService encodeService) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
        this.additionService = additionService;
        this.qdrantService = qdrantService;
        this.descriptionService = descriptionService;
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

//        Item existing = itemRepository.findByTitleIgnoreCase(item.getTitle());
//        if (existing != null) {
//            throw new IllegalArgumentException("Такая услуга уже есть в базе!");
//        }

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
     * @throws IllegalArgumentException Если услуга с указанным идентификатором не найдена.
     */
    public Item getItemById(long itemId) {
        Item foundItem = itemRepository.findById(itemId);
        if (foundItem == null) {
            throw new IllegalArgumentException("Услуга с id " + itemId + " не найдена");
        } else {
            log.debug("Найдена услуга с id {}", itemId);
            return foundItem;
        }
    }

    /**
     * Удаляет услугу по ее идентификатору.
     * Если к услуге привязано дополнение, удаляет и его.
     *
     * @param itemId Идентификатор услуги.
     * @throws IllegalArgumentException Если услуга с указанным идентификатором не найдена.
     */
    public void deleteItem(long itemId) {
        Item foundItem = itemRepository.findById(itemId);
        if (foundItem == null) {
            throw new IllegalArgumentException("Услуга с id " + itemId + " не найдена");
        } else {
            if (foundItem.getAdditions().size() != 0)
                foundItem.getAdditions().stream()
                        .map(Addition::getId)
                        .forEach(additionService::deleteAddition);
            itemRepository.deleteById(itemId);
            deleteQdrantTitle(foundItem);
            log.info("Удалена услуга с id {}", itemId);
        }
    }

    /**
     * Добавляет услугу в категорию.
     *
     * @param itemId     Идентификатор услуги.
     * @param categoryId Идентификатор категории.
     * @throws IllegalArgumentException Если услуга или категория с указанным идентификатором не найдены.
     */
    public void addToCategory(long itemId, long categoryId) {
        Item item = itemRepository.findById(itemId);
        Category category = categoryService.getCategoryById(categoryId);

        if (category.getChildrenCategories().isEmpty()) {
            if (item == null)
                throw new IllegalArgumentException("Услуга с id " + itemId + " отсутствует");
            else if (item.getCategory() == null) {
                item.setCategory(category);
                itemRepository.save(item);
                TitleRequest titleRequest = new TitleRequest(formatTitle(item));
                flaskApiVectorSearchService.deleteTitle(titleRequest);
                AddTitleRequest addTitleRequest = new AddTitleRequest(formatTitle(item), item.getId());
                flaskApiVectorSearchService.addTitle(addTitleRequest);
                log.info("Услуга с id {} добавлена в категорию с id {}", itemId, categoryId);
            } else
                log.error("Услуга с id {} уже в другой категории!", itemId);
        } else
            log.error("В категории с id {} есть подкатегории - услугу можно добавить только в конечную подкатегорию!", categoryId);
    }

    /**
     * Удаляет услугу из категории.
     *
     * @param itemId Идентификатор услуги.
     * @throws IllegalArgumentException Если услуга с указанным идентификатором не найдена.
     */
    public void removeFromCategory(long itemId) {
        Item item = itemRepository.findById(itemId);

        if (item == null)
            throw new IllegalArgumentException("Услуга с id " + itemId + " отсутствует");
        else if (item.getCategory() != null) {
            TitleRequest titleRequest = new TitleRequest(formatTitle(item));
            flaskApiVectorSearchService.deleteTitle(titleRequest);
            item.setCategory(null);
            itemRepository.save(item);
            AddTitleRequest addTitleRequest = new AddTitleRequest(formatTitle(item), item.getId());
            flaskApiVectorSearchService.addTitle(addTitleRequest);
            log.info("Услуга с id {} удалена из категории", itemId);
        } else
            log.error("Услуга с id {} не относится ни к одной из категорий!", itemId);
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
     * @param attempts попытки синхронизации БД
     * @return страница найденных услуг
     */
    public Page<Item> findItemsByTitle(String title, Pageable pageable, int attempts) {
        List<String> request = new ArrayList<>();
        request.add(title);
        List<Long> result = flaskApiVectorSearchService.getEmbeddings(request);

        List<Item> items = new ArrayList<>();
        for (Long element: result) {
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
            attempts += 1;
            if (attempts < 3) {
                return findItemsByTitle(title, pageable, attempts);
            } else
                throw new ItemsFindException("Проблема не в синхронизации БД!");
        } else {
            return page;
        }
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
    public void updateDescriptionToItem(long itemId, String desc, boolean enableAudio) throws IOException {
        Item item = getItemById(itemId);
        if (item != null) {
            deleteQdrantTitle(item);
            item.setDescription(desc);
            item.setDescriptionHash(encodeService.generateHashForAudio(desc));
            item.getAudio().clear();
            if (enableAudio) {
                generateDescriptionAudio(item);
            }
            itemRepository.save(item);
            addQdrantTitle(item);
            log.info("Описание обновлено для услуги с id {}", itemId);
        }
    }

    /**
     * Обновляет ссылку на GIF-анимацию услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param gifLink Новая ссылка на GIF услуги.
     */
    public void updateGifLinkToItem(long itemId, String gifLink) {
        Item item = getItemById(itemId);
        if (item != null) {
            item.setGifLink(gifLink);
            itemRepository.save(item);
            log.info("Ссылка на GIF обновлена для услуги с id {}", itemId);
        }
    }

    /**
     * Обновляет ссылку на главную иконку услуги.
     *
     * @param itemId Идентификатор услуги.
     * @param icon   Новая ссылка на главную иконку.
     */
    public void updateMainIconToItem(long itemId, String icon) {
        Item item = getItemById(itemId);
        if (item != null) {
            item.setMainIconLink(icon);
            itemRepository.save(item);
            log.info("Ссылка на главную иконку обновлена для услуги с id {}", itemId);
        }
    }

    /**
     * Обновляет ссылку на GIF-превью услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param gifPreview Новая ссылка на GIF превью услуги.
     */
    public void updateGifPreviewToItem(long itemId, String gifPreview) {
        Item item = getItemById(itemId);
        if (item != null) {
            item.setGifPreview(gifPreview);
            itemRepository.save(item);
            log.info("Ссылка на GIF-превью обновлена для услуги с id {}", itemId);
        }
    }

    /**
     * Добавляет иконку для услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param iconLink Иконка.
     */
    public void addIcon(long itemId, String iconLink) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (!item.getIconLinks().contains(iconLink)) {
                item.getIconLinks().add(iconLink);
                itemRepository.save(item);
                log.info("Добавлена иконка для услуги с id {}", itemId);
            }
            else
                log.warn("Иконка для услуги с id {} уже была добавлена раннее!", itemId);
        }
    }

    /**
     * Удаляет иконку у услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param iconLink Иконка.
     */
    public void removeIcon(long itemId, String iconLink) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (item.getIconLinks().contains(iconLink)) {
                item.getIconLinks().remove(iconLink);
                itemRepository.save(item);
                log.info("Удалена иконка для услуги с id {}", itemId);
            }
        }
    }

    /**
     * Чистит иконки у услуги.
     *
     * @param itemId  Идентификатор услуги.
     */
    public void clearIcons(long itemId) {
        Item item = getItemById(itemId);
        if (item != null) {
            item.getIconLinks().clear();
            itemRepository.save(item);
            log.info("Очищены иконки у услуги {}", itemId);
        }
    }

    /**
     * Добавляет ключевое слово для услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param keyword Слово.
     */
    public void addKeyword(long itemId, String keyword) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (!item.getKeywords().contains(keyword)) {
                deleteQdrantTitle(item);
                item.getKeywords().add(keyword);
                itemRepository.save(item);
                addQdrantTitle(item);
                log.info("Добавлено ключевое слово для услуги с id {}", itemId);
            }
            else
                log.warn("Ключевое слово для услуги с id {} уже было добавлено раннее!", itemId);
        }
    }

    /**
     * Удаляет ключевое слово у услуги.
     *
     * @param itemId  Идентификатор услуги.
     * @param keyword Слово.
     */
    public void removeKeyword (long itemId, String keyword) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (item.getKeywords().contains(keyword)) {
                deleteQdrantTitle(item);
                item.getKeywords().remove(keyword);
                itemRepository.save(item);
                addQdrantTitle(item);
                log.info("Удалено ключевое слово для услуги с id {}", itemId);
            }
        }
    }

    /**
     * Чистит ключевые слова у услуги.
     *
     * @param itemId  Идентификатор услуги.
     */
    public void clearKeywords(long itemId) {
        Item item = getItemById(itemId);
        if (item != null) {
            deleteQdrantTitle(item);
            item.getKeywords().clear();
            itemRepository.save(item);
            addQdrantTitle(item);
            log.info("Очищены ключевые слова у услуги {}", itemId);
        }
    }

    /**
     * Удаляет услугу из базы Qdrant.
     *
     * @param item  Услуга.
     */
    private void deleteQdrantTitle(Item item) {
        TitleRequest titleRequest;
        if (item.getCategory() != null)
            titleRequest = new TitleRequest(formatTitle(item));
        else
            titleRequest = new TitleRequest(formatTitle(item));
        flaskApiVectorSearchService.deleteTitle(titleRequest);
    }

    /**
     * Добавляет услугу в базу Qdrant.
     *
     * @param item  Услуга.
     */
    private void addQdrantTitle(Item item) {
        AddTitleRequest addTitleRequest;
        if (item.getCategory() != null)
            addTitleRequest = new AddTitleRequest(formatTitle(item), item.getId());
        else
            addTitleRequest = new AddTitleRequest(formatTitle(item), item.getId());
        flaskApiVectorSearchService.addTitle(addTitleRequest);
    }

    /**
     * Формирует строку для вектора услуги в нужном формате.
     *
     * @param item  Услуга.
     * @return Отформатированная строка.
     */
    private String formatTitle(Item item) {
        String keywords;
        if (item.getKeywords().isEmpty())
            keywords = "";
        else
            keywords  = String.join(" ", item.getKeywords());

        String category = item.getCategory() != null ? item.getCategory().getTitle() : "";
        String description = item.getDescription();

        if (!category.isEmpty()) {
            return item.getTitle() + " " + keywords + " " + category + " " + description;
        } else {
            return item.getTitle() + " " + keywords + " " + description;
        }
    }

    /**
     * Генерирует аудио заголовка услуги.
     *
     * @param itemId  Идентификатор услуги.
     */
    public void generateTitleAudio(long itemId) throws IOException {
        Item item = getItemById(itemId);
        if (item != null) {
            if (item.getTitleAudio() == null) {
                String titleAudio = speechService.generateAudio(item.getTitle());
                item.setTitleAudio(titleAudio);
                itemRepository.save(item);
                log.info("Сгенерировано аудио заголовка для услуги с id {}", itemId);
            }
            else
                log.warn("У услуги {} уже есть аудио заголовка!", itemId);
        }
    }

    /**
     * Удаляет аудио заголовка услуги.
     *
     * @param itemId  Идентификатор услуги.
     */
    public void removeTitleAudio(long itemId) {
        Item item = getItemById(itemId);
        if (item != null) {
            if (item.getTitleAudio() == null) {
                log.warn("У услуги {} нет аудио заголовка!", itemId);
            }
            else {
                item.setTitleAudio(null);
                itemRepository.save(item);
                log.info("У услуги {} удалено аудио заголовка", itemId);
            }
        }
    }

    /**
     * Генерирует аудио для описания услуги.
     * По хэш-функции проверяет, не было ли раннее сгенерировано такое аудио.
     *
     * @param item услуга
     * @throws IOException исключение
     */
    private void generateDescriptionAudio(Item item) throws IOException {
        Page<Item> itemsWithSameDescriptionRequest = itemRepository.findByHashAndAudioExistence(item.getDescriptionHash(), PageRequest.of(0, 1));
        if (itemsWithSameDescriptionRequest.hasContent()) {
            Item itemWithSameDescription = itemsWithSameDescriptionRequest.getContent().get(0);
            List<String> sameAudio = itemWithSameDescription.getAudio();
            for (String audioLink : sameAudio) {
                item.getAudio().add(audioLink);
            }
        } else {
            String[] descriptionBlocks = descriptionService.splitDescription(item.getDescription());
            for (String block : descriptionBlocks) {
                String audioLink = speechService.generateAudio(block);
                item.getAudio().add(audioLink);
            }
        }
    }

    // TODO remove after DB adaptation
    public void setAudioAndHash() {
        Pageable pageableCheckQuantity = PageRequest.of(0, 999);
        Page<Item> itemsPage = getAllItemsInBase(pageableCheckQuantity);
        List<Item> items = itemsPage.getContent();
        for (Item i: items) {
            i.setAudio(new ArrayList<>());
            i.setDescriptionHash(encodeService.generateHashForAudio(i.getDescription()));
            itemRepository.save(i);
        }
    }
}
