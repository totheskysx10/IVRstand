package com.good.ivrstand.app;

import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final AdditionService additionService;

    @Autowired
    public ItemService(ItemRepository itemRepository, CategoryService categoryService, AdditionService additionService) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
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
            log.info("Найдена услуга с id {}", itemId);
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
            item.setCategory(null);
            itemRepository.save(item);
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
        return itemRepository.findByTitle(title, pageable);
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
     * Ищет услуги по категории и заголовку, с поддержкой пагинации.
     *
     * @param categoryId Категория для поиска.
     * @param title      Часть заголовка для поиска.
     * @param pageable   Настройки пагинации.
     * @return Страница найденных услуг.
     */
    public Page<Item> findItemsByTitleAndCategory(long categoryId, String title, Pageable pageable) {
        return itemRepository.findByTitleAndCategoryId(categoryId, title, pageable);
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
            item.setDescription(desc);
            itemRepository.save(item);
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
     * Находит услуги, похожие на заданный заголовок, путем поиска ключевых слов в базе данных.
     * Поиск осуществляется путем разделения заголовка на слова и поиска каждого слова
     * индивидуально в заголовках и ключевых словах услуг. Результаты поиска агрегируются
     * и обрабатываются для определения наиболее релевантного ключевого слова. Затем услуги,
     * связанные с этим ключевым словом, возвращаются в пагинированном виде.
     *
     * @param title    Заголовок, для которого требуется найти похожие услуги.
     * @param pageable Информация о пагинации.
     * @return Страница с услугами, считающимися похожими на заданный заголовок.
     */
    public Page<Item> findSimilarItems(String title, Pageable pageable) {

        String[] wordArray = title.split("\\s+");

        List<Item> searchResults = new ArrayList<>();
        for (String word : wordArray) {
            if (word.length() >= 3) {
                Page<Item> pageTitle = itemRepository.findByTitleContainingIgnoreCase(word, PageRequest.of(0, Integer.MAX_VALUE));
                List<Item> itemsTitle = pageTitle.getContent();
                Page<Item> pageKey = itemRepository.findByKeyWordContainingIgnoreCase(word, PageRequest.of(0, Integer.MAX_VALUE));
                List<Item> itemsKey = pageKey.getContent();
                searchResults.addAll(itemsTitle);
                searchResults.addAll(itemsKey);
            }
        }

        String searchWord = null;
        Map<String, Integer> frequencies = new HashMap<>();

        for (String word : wordArray) {
            for (Item item : searchResults) {
                if (calculateLevenshtein(item.getKeyWord(), word) < 4) {
                    searchWord = item.getKeyWord();
                    break;
                } else {
                    if (frequencies.containsKey(item.getKeyWord())) {
                        int currentFrequency = frequencies.get(item.getKeyWord());
                        frequencies.put(item.getKeyWord(), currentFrequency + 1);
                    } else {
                        frequencies.put(item.getKeyWord(), 1);
                    }
                }
            }
        }

        if (searchWord == null) {
            Map.Entry<String, Integer> entryWithMaxValue = frequencies.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow();
            searchWord = entryWithMaxValue.getKey();
        }

        return itemRepository.findByKeyWordIgnoreCase(searchWord, pageable);
    }

    private int calculateLevenshtein(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];

    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }
}
