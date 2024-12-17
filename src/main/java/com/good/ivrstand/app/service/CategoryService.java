package com.good.ivrstand.app.service;

import com.good.ivrstand.app.repository.CategoryRepository;
import com.good.ivrstand.app.service.externinterfaces.FlaskApiVectorSearchService;
import com.good.ivrstand.exception.CategoryUpdateException;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.ItemCategoryAddDeleteException;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Сервис для работы с категориями
 */
@Component
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final FlaskApiVectorSearchService flaskApiVectorSearchService;
    private final SpeechService speechService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, FlaskApiVectorSearchService flaskApiVectorSearchService, SpeechService speechService) {
        this.categoryRepository = categoryRepository;
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
        this.speechService = speechService;
    }

    /**
     * Создает новую категорию.
     *
     * @param category Создаваемая категория.
     * @return Сохраненная категория.
     * @throws IllegalArgumentException Если переданная категория равна null.
     * @throws RuntimeException         Если возникла ошибка при создании категории.
     */
    public Category createCategory(Category category, boolean enableAudio) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }

        try {
            if (enableAudio) {
                String titleAudio = speechService.generateAudio(category.getTitle());
                category.setTitleAudio(titleAudio);
            }
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
     * @throws CategoryNotFoundException Если категория с указанным идентификатором не найдена.
     */
    public Category getCategoryById(long categoryId) throws CategoryNotFoundException {
        Category foundCategory = categoryRepository.findById(categoryId);
        if (foundCategory == null) {
            throw new CategoryNotFoundException("Категория с id " + categoryId + " не найдена");
        } else {
            log.debug("Найдена категория с id {}", categoryId);
            return foundCategory;
        }
    }

    /**
     * Удаляет категорию по ее идентификатору,
     * услуги из неё остаются нераспределёнными по категориям,
     * дочерние категории становятся детьми их дедушки
     *
     * @param categoryId Идентификатор категории.
     */
    public void deleteCategory(long categoryId) throws CategoryNotFoundException {
        Category foundCategory = getCategoryById(categoryId);
        for (Item i : foundCategory.getItemsInCategory()) {
            TitleRequest titleRequest = new TitleRequest(i.getTitle() + " " + i.getCategory().getTitle() + " " + i.getDescription());
            flaskApiVectorSearchService.deleteTitle(titleRequest);
            i.setCategory(null);
            AddTitleRequest addTitleRequest = new AddTitleRequest(i.getTitle() + " " + i.getDescription(), i.getId());
            flaskApiVectorSearchService.addTitle(addTitleRequest);
        }
        for (Category c : foundCategory.getChildrenCategories()) {
            c.setParentCategory(c.getParentCategory().getParentCategory());
        }
        categoryRepository.save(foundCategory);
        categoryRepository.deleteById(categoryId);
        log.info("Удалена категория с id {}", categoryId);
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
     * @param categoryId Идентификатор подкатегории.
     * @param parentId   Идентификатор категорию.
     * @throws ItemCategoryAddDeleteException Если подкатегория уже в другой категории или категория имеет услуги
     * @throws IllegalArgumentException Если идентификаторы категорий совпадают
     */
    public void addToCategory(long categoryId, long parentId) throws ItemCategoryAddDeleteException, CategoryNotFoundException {
        if (categoryId == parentId) {
            throw new IllegalArgumentException("Идентификаторы категорий совпадают: " + categoryId);
        }

        Category category = getCategoryById(categoryId);
        Category parent = getCategoryById(parentId);

        if (parent.getItemsInCategory().isEmpty()) {
            if (category.getParentCategory() == null) {
                category.setParentCategory(parent);
                categoryRepository.save(category);
                categoryRepository.save(parent);
                log.info("Подкатегория с id {} добавлена в категорию с id {}", categoryId, parentId);
            } else
                throw new ItemCategoryAddDeleteException(String.format("Подкатегория с id %s уже в другой категории!", categoryId));
        } else
            throw new ItemCategoryAddDeleteException(String.format("В категории с id %s есть услуги - продолжение дерева невозможно!", parentId));
    }

    /**
     * Удаляет подкатегорию из категории.
     *
     * @param categoryId Идентификатор подкатегории.
     * @throws ItemCategoryAddDeleteException Если подкатегория не относится к категории
     */
    public void removeFromCategory(long categoryId) throws ItemCategoryAddDeleteException, CategoryNotFoundException {
        Category category = getCategoryById(categoryId);

        if (category.getParentCategory() != null) {
            category.setParentCategory(null);
            categoryRepository.save(category);
            log.info("Подкатегория с id {} удалена из категории", categoryId);
        } else
            throw new ItemCategoryAddDeleteException(String.format("Подкатегория с id %s не относится ни к одной из категорий!", categoryId));
    }

    /**
     * Обновляет ссылку на GIF-анимацию категории.
     *
     * @param categoryId Идентификатор категории.
     * @param gifLink    Новая ссылка на GIF.
     */
    public void updateGifLinkToCategory(long categoryId, String gifLink) throws CategoryNotFoundException {
        Category category = getCategoryById(categoryId);
        category.setGifLink(gifLink);
        categoryRepository.save(category);
        log.info("Ссылка на GIF обновлена для категории с id {}", categoryId);
    }

    /**
     * Обновляет ссылку на GIF-превью категории.
     *
     * @param categoryId Идентификатор категории.
     * @param gifPreview Новая ссылка на GIF превью.
     */
    public void updateGifPreviewToCategory(long categoryId, String gifPreview) throws CategoryNotFoundException {
        Category category = getCategoryById(categoryId);
        category.setGifPreview(gifPreview);
        categoryRepository.save(category);
        log.info("Ссылка на GIF-превью обновлена для категории с id {}", categoryId);
    }

    /**
     * Обновляет ссылку на главную иконку категории.
     *
     * @param categoryId Идентификатор категории.
     * @param icon       Новая ссылка на главную иконку.
     */
    public void updateMainIconToCategory(long categoryId, String icon) throws CategoryNotFoundException {
        Category category = getCategoryById(categoryId);
        category.setMainIconLink(icon);
        categoryRepository.save(category);
        log.info("Ссылка на главную иконку обновлена для категории с id {}", categoryId);
    }

    /**
     * Генерирует аудио заголовка категории.
     *
     * @param categoryId Идентификатор категории.
     * @throws CategoryUpdateException если аудио заголовка уже есть у категории
     */
    public void generateTitleAudio(long categoryId) throws IOException, FileDuplicateException, CategoryUpdateException, CategoryNotFoundException {
        Category category = getCategoryById(categoryId);
        if (category.getTitleAudio() == null) {
            String titleAudio = speechService.generateAudio(category.getTitle());
            category.setTitleAudio(titleAudio);
            categoryRepository.save(category);
            log.info("Сгенерировано аудио заголовка для категории с id {}", categoryId);
        } else
            throw new CategoryUpdateException(String.format("У категории %s уже есть аудио заголовка!", categoryId));
    }

    /**
     * Удаляет аудио заголовка категории.
     *
     * @param categoryId Идентификатор категории.
     * @throws CategoryUpdateException если аудио заголовка уже нет у категории
     */
    public void removeTitleAudio(long categoryId) throws CategoryUpdateException, CategoryNotFoundException {
        Category category = getCategoryById(categoryId);
        if (category.getTitleAudio() == null) {
            throw new CategoryUpdateException(String.format("У категории %s нет аудио заголовка!", categoryId));
        } else {
            category.setTitleAudio(null);
            categoryRepository.save(category);
            log.info("У категории {} удалено аудио заголовка", categoryId);
        }
    }
}
