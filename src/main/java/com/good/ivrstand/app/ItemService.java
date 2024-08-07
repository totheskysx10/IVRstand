package com.good.ivrstand.app;

import com.good.ivrstand.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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

    @Autowired
    public ItemService(ItemRepository itemRepository, CategoryService categoryService, FlaskApiVectorSearchService flaskApiVectorSearchService, AdditionService additionService) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
        this.additionService = additionService;
    }

    /**
     * Создает новую услугу.
     *
     * @param item Создаваемая услуга.
     * @return Сохраненная услуга.
     * @throws IllegalArgumentException Если переданная услуга равна null.
     * @throws RuntimeException         Если возникла ошибка при создании услуги.
     */
    public Item createItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Услуга не может быть null");
        }

//        Item existing = itemRepository.findByTitleIgnoreCase(item.getTitle());
//        if (existing != null) {
//            throw new IllegalArgumentException("Такая услуга уже есть в базе!");
//        }

        try {
            Item savedItem = itemRepository.save(item);
            AddTitleRequest addTitleRequest = new AddTitleRequest(savedItem.getTitle() + " " + savedItem.getDescription(), savedItem.getId());
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
            TitleRequest titleRequest;
            if (foundItem.getCategory() != null) {
                titleRequest = new TitleRequest(foundItem.getTitle() + " " + foundItem.getCategory().getTitle() + " " + foundItem.getDescription());
            } else {
                titleRequest = new TitleRequest(foundItem.getTitle() + " " + foundItem.getDescription());
            }
            flaskApiVectorSearchService.deleteTitle(titleRequest);
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
            else if (category == null)
                throw new IllegalArgumentException("Категория с id " + categoryId + " отсутствует");
            else if (item.getCategory() == null) {
                item.setCategory(category);
                itemRepository.save(item);
                TitleRequest titleRequest = new TitleRequest(item.getTitle() + " " + item.getDescription());
                flaskApiVectorSearchService.deleteTitle(titleRequest);
                AddTitleRequest addTitleRequest = new AddTitleRequest(item.getTitle() + " " + category.getTitle() + " " + item.getDescription(), item.getId());
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
            TitleRequest titleRequest = new TitleRequest(item.getTitle() + " " + item.getCategory().getTitle() + " " + item.getDescription());
            flaskApiVectorSearchService.deleteTitle(titleRequest);
            item.setCategory(null);
            itemRepository.save(item);
            AddTitleRequest addTitleRequest = new AddTitleRequest(item.getTitle() + " " + item.getDescription(), item.getId());
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
     *
     * @param title    Часть заголовка для поиска.
     * @param pageable Настройки пагинации.
     * @return Страница найденных услуг.
     */
    public Page<Item> findItemsByTitle(String title, Pageable pageable) {
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
    public void updateDescriptionToItem(long itemId, String desc) {
        Item item = getItemById(itemId);
        if (item != null) {
            TitleRequest titleRequest;
            if (item.getCategory() != null)
                titleRequest = new TitleRequest(item.getTitle() + " " + item.getCategory().getTitle() + " " + item.getDescription());
            else
                titleRequest = new TitleRequest(item.getTitle() + " " + item.getDescription());
            flaskApiVectorSearchService.deleteTitle(titleRequest);
            item.setDescription(desc);
            itemRepository.save(item);
            AddTitleRequest addTitleRequest;
            if (item.getCategory() != null)
                addTitleRequest = new AddTitleRequest(item.getTitle() + " " + item.getCategory().getTitle() + " " + item.getDescription(), item.getId());
            else
                addTitleRequest = new AddTitleRequest(item.getTitle() + " " + item.getDescription(), item.getId());
            flaskApiVectorSearchService.addTitle(addTitleRequest);
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
}
