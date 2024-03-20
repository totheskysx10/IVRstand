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
     * услуги из неё остаются нераспределёнными по категориям.
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
            categoryRepository.save(foundCategory);
            categoryRepository.deleteById(categoryId);
            log.info("Удалёна категория с id {}", categoryId);
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
        return categoryRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
