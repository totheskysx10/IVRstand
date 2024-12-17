package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.CategoryRepository;
import com.good.ivrstand.app.service.CategoryService;
import com.good.ivrstand.app.service.SpeechService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import com.good.ivrstand.exception.CategoryUpdateException;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.ItemCategoryAddDeleteException;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SpeechService speechService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void testCreateCategory() throws IOException, FileDuplicateException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(speechService.generateAudio("Test Title")).thenReturn("audioLink");
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(category, true);

        assertEquals("audioLink", result.getTitleAudio());
        verify(speechService).generateAudio("Test Title");
        verify(categoryRepository).save(category);
    }

    @Test
    void testGetCategoryById() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(category);

        Category result = categoryService.getCategoryById(1L);

        assertEquals(category, result);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void testGetCategoryByIdNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategoryById(1L);
        });

        assertEquals("Категория с id 1 не найдена", exception.getMessage());
    }

    @Test
    void testDeleteCategory() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(category);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
        verify(categoryRepository).save(category);
    }

    @Test
    void testAddToCategory() throws ItemCategoryAddDeleteException, CategoryNotFoundException {
        Category parent = new Category(1L,
                "Parent",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Category child = new Category(2L,
                "Child",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(parent);
        when(categoryRepository.findById(2L)).thenReturn(child);

        categoryService.addToCategory(2L, 1L);

        assertEquals(parent, child.getParentCategory());
        verify(categoryRepository).save(parent);
        verify(categoryRepository).save(child);
    }

    @Test
    void testAddToCategoryWhenCategoryAlreadyHasParent() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        Category parent = new Category(1L,
                "Parent",
                List.of(item),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Category child = new Category(2L,
                "Child",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(parent);
        when(categoryRepository.findById(2L)).thenReturn(child);

        Exception exception = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            categoryService.addToCategory(2L, 1L);
        });

        assertEquals("В категории с id 1 есть услуги - продолжение дерева невозможно!", exception.getMessage());
    }

    @Test
    void testAddToCategoryHasItems() {
        Category parent = new Category(1L,
                "Parent",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Category category1 = new Category(2L,
                "Child",
                new ArrayList<>(),
                new ArrayList<>(),
                parent,
                "preview",
                "link",
                "icon",
                "audio");

        Category category2 = new Category(3L,
                "Child1",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(parent);
        when(categoryRepository.findById(2L)).thenReturn(category1);
        when(categoryRepository.findById(3L)).thenReturn(category2);

        Exception exception = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            categoryService.addToCategory(2L, 3L);
        });

        assertEquals("Подкатегория с id 2 уже в другой категории!", exception.getMessage());
    }

    @Test
    void testAddToCategorySameId() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.addToCategory(1L, 1L);
        });

        assertEquals("Идентификаторы категорий совпадают: 1", e.getMessage());
    }

    @Test
    void testRemoveFromCategory() throws ItemCategoryAddDeleteException, CategoryNotFoundException {
        Category category = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Category category2 = new Category(2L,
                "Child",
                new ArrayList<>(),
                new ArrayList<>(),
                category,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(category);
        when(categoryRepository.findById(2L)).thenReturn(category2);

        categoryService.removeFromCategory(2L);

        assertNull(category2.getParentCategory());
        verify(categoryRepository).save(category2);
    }

    @Test
    void testRemoveFromCategoryNotInCategory() {
        Category category = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(categoryRepository.findById(1L)).thenReturn(category);

        Exception e = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            categoryService.removeFromCategory(1L);
        });

        assertEquals("Подкатегория с id 1 не относится ни к одной из категорий!", e.getMessage());
    }

    @Test
    void testGenerateTitleAudio() throws IOException, CategoryUpdateException, FileDuplicateException, CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                null);

        when(categoryRepository.findById(1L)).thenReturn(category);
        when(speechService.generateAudio("Test Title")).thenReturn("audioLink");

        categoryService.generateTitleAudio(1L);

        assertEquals("audioLink", category.getTitleAudio());
        verify(categoryRepository).save(category);
    }

    @Test
    void testGenerateTitleAudioAlreadyHas() {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "existingAudio");

        when(categoryRepository.findById(1L)).thenReturn(category);

        Exception e = assertThrows(CategoryUpdateException.class, () -> {
            categoryService.generateTitleAudio(1L);
        });

        assertEquals("У категории 1 уже есть аудио заголовка!", e.getMessage());
    }

    @Test
    void removeTitleAudio() throws CategoryUpdateException, CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "existingAudio");

        when(categoryRepository.findById(1L)).thenReturn(category);

        categoryService.removeTitleAudio(1L);

        assertNull(category.getTitleAudio());
        verify(categoryRepository).save(category);
    }

    @Test
    void removeTitleAudioNoAudio() {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                null);

        when(categoryRepository.findById(1L)).thenReturn(category);

        Exception e = assertThrows(CategoryUpdateException.class, () -> {
            categoryService.removeTitleAudio(1L);
        });

        assertEquals("У категории 1 нет аудио заголовка!", e.getMessage());
    }

    @Test
    void testUpdateGifLinkToCategory() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                null);

        when(categoryRepository.findById(1L)).thenReturn(category);

        categoryService.updateGifLinkToCategory(1L, "newGifLink");

        assertEquals("newGifLink", category.getGifLink());
        verify(categoryRepository).save(category);
    }

    @Test
    void testUpdateGifPreviewToCategory() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                null);

        when(categoryRepository.findById(1L)).thenReturn(category);

        categoryService.updateGifPreviewToCategory(1L, "newGifLink");

        assertEquals("newGifLink", category.getGifPreview());
        verify(categoryRepository).save(category);
    }

    @Test
    void testUpdateMainIconToCategory() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "Test Title",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                null);

        when(categoryRepository.findById(1L)).thenReturn(category);

        categoryService.updateMainIconToCategory(1L, "newMainIcon");

        assertEquals("newMainIcon", category.getMainIconLink());
        verify(categoryRepository).save(category);
    }
}
