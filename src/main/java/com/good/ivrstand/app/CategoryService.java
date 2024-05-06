package com.good.ivrstand.app;

import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Сервисный класс для работы с категориями (Categories).
 * Обеспечивает операции создания, получения, обновления и удаления категорий,
 * а также поиск категорий по различным критериям.
 */
@Component
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Создает новую категорию.
     *
     * @param category Создаваемая категория.
     * @return Сохраненная категория.
     * @throws IllegalArgumentException Если переданная категория равна null.
     * @throws RuntimeException         Если возникла ошибка при создании категории.
     */
    public Category createCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }

//        Category existing = categoryRepository.findByTitleIgnoreCase(category.getTitle());
//        if (existing != null) {
//            throw new IllegalArgumentException("Такая категория уже есть в базе!");
//        }

        try {
            Category savedCategory = categoryRepository.save(category);
            log.info("Создана категория с id {}", savedCategory.getId());
            return savedCategory;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании категории", e);
        }
    }

    /**
     * Получает категорию по ее идентификатору.
     *
     * @param categoryId Идентификатор категории.
     * @return Найденная категория.
     * @throws IllegalArgumentException Если категория с указанным идентификатором не найдена.
     */
    public Category getCategoryById(long categoryId) {
        Category foundCategory = categoryRepository.findById(categoryId);
        if (foundCategory == null) {
            throw new IllegalArgumentException("Категория с id " + categoryId + " не найдена");
        }
        else {
            log.info("Найдена категория с id {}", categoryId);
            return foundCategory;
        }
    }

    /**
     * Удаляет категорию по ее идентификатору,
     * услуги из неё остаются нераспределёнными по категориям,
     * дочерние категории становятся детьми их дедушки
     *
     * @param categoryId Идентификатор категории.
     * @throws IllegalArgumentException Если категория с указанным идентификатором не найдена.
     */
    public void deleteCategory(long categoryId) {
        Category foundCategory = categoryRepository.findById(categoryId);
        if (foundCategory == null) {
            throw new IllegalArgumentException("Категория с id " + categoryId + " не найдена");
        } else {
            for (Item i: foundCategory.getItemsInCategory()) {
                i.setCategory(null);
            }
            for (Category c: foundCategory.getChildrenCategories()) {
                c.setParentCategory(c.getParentCategory().getParentCategory());
            }
            categoryRepository.save(foundCategory);
            categoryRepository.deleteById(categoryId);
            log.info("Удалена категория с id {}", categoryId);
        }
    }

    /**
     * Получает все категории из базы данных, с поддержкой пагинации.
     *
     * @param pageable Настройки пагинации.
     * @return Страница категорий.
     */
    public Page<Category> getAllCategoriesInBase(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    /**
     * Ищет категории по заголовку, с поддержкой пагинации.
     *
     * @param title    Часть заголовка для поиска.
     * @param pageable Настройки пагинации.
     * @return Страница найденных категорий.
     */
    public Page<Category> findCategoriesByTitle(String title, Pageable pageable) {
        return categoryRepository.findByTitle(title, pageable);
    }

    /**
     * Ищет нераспределённые категории (без детей и родителя), с поддержкой пагинации.
     *
     * @param pageable Настройки пагинации.
     * @return Страница найденных категорий.
     */
    public Page<Category> findUnallocatedCategories(Pageable pageable) {
        return categoryRepository.findUnallocatedCategories(pageable);
    }

    /**
     * Ищет главные категории, с поддержкой пагинации.
     *
     * @param pageable Настройки пагинации.
     * @return Страница найденных категорий.
     */
    public Page<Category> findMainCategories(Pageable pageable) {
        return categoryRepository.findMainCategories(pageable);
    }

    /**
     * Добавляет подкатегорию в категорию.
     *
     * @param categoryId     Идентификатор подкатегории.
     * @param parentId Идентификатор категорию.
     * @throws IllegalArgumentException Если подкатегория или категория с указанным идентификатором не найдены.
     */
    public void addToCategory(long categoryId, long parentId) {
        if (categoryId == parentId) {
            throw new IllegalArgumentException("Идентификаторы категорий совпадают: " + categoryId);
        }

        Category category = categoryRepository.findById(categoryId);
        Category parent = getCategoryById(parentId);

        if (parent.getItemsInCategory().isEmpty()) {
            if (category == null)
                throw new IllegalArgumentException("Категория с id " + categoryId + " отсутствует");
            else if (parent == null)
                throw new IllegalArgumentException("Категория с id " + parentId + " отсутствует");
            else if (category.getParentCategory() == null) {
                category.setParentCategory(parent);
                categoryRepository.save(category);
                categoryRepository.save(parent);
                log.info("Подкатегория с id {} добавлена в категорию с id {}", categoryId, parentId);
            } else
                log.error("Подкатегория с id {} уже в другой категории!", categoryId);
        } else
            log.error("В категории с id {} есть услуги - продолжение дерева невозможно!", parentId);
    }

    /**
     * Удаляет подкатегорию из категории.
     *
     * @param categoryId     Идентификатор подкатегории.
     * @throws IllegalArgumentException Если подкатегория с указанным идентификатором не найдена.
     */
    public void removeFromCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId);

        if (category == null)
            throw new IllegalArgumentException("Услуга с id " + categoryId + " отсутствует");
        else if (category.getParentCategory() != null) {
            category.setParentCategory(null);
            categoryRepository.save(category);
            log.info("Подкатегория с id {} удалена из категории", categoryId);
        } else
            log.error("Подкатегория с id {} не относится ни к одной из категорий!", categoryId);
    }

    /**
     * Обновляет ссылку на GIF-анимацию категории.
     *
     * @param categoryId Идентификатор категории.
     * @param gifLink   Новая ссылка на GIF.
     */
    public void updateGifLinkToCategory(long categoryId, String gifLink) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            category.setGifLink(gifLink);
            categoryRepository.save(category);
            log.info("Ссылка на GIF обновлена для категории с id {}", categoryId);
        }
    }

    /**
     * Обновляет ссылку на GIF-превью категории.
     *
     * @param categoryId Идентификатор категории.
     * @param gifPreview   Новая ссылка на GIF превью.
     */
    public void updateGifPreviewToCategory(long categoryId, String gifPreview) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            category.setGifPreview(gifPreview);
            categoryRepository.save(category);
            log.info("Ссылка на GIF-превью обновлена для категории с id {}", categoryId);
        }
    }
}
