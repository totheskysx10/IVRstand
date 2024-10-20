package com.good.ivrstand;

import com.good.ivrstand.app.CategoryService;
import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThrows;

@SpringBootTest
public class CategoryServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testCreateCategory() {
        Category category = new Category();
        category.setTitle("TestTitle");
        Category savedCategory = categoryService.createCategory(category, false);
        assertNotNull(savedCategory.getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetCategoryById() {
        Category retrievedCategory = categoryService.getCategoryById(1);
        assertNotNull(retrievedCategory);
        assertEquals(1, retrievedCategory.getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetCategoryByIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryById(4);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testDeleteCategory() {
        itemService.addToCategory(1, 1);
        assertEquals(categoryService.getCategoryById(1), itemService.getItemById(1).getCategory());

        categoryService.deleteCategory(1);

        assertEquals(null, itemService.getItemById(1).getCategory());

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryById(1);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetAllCategoriesInBase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoriessPage = categoryService.getAllCategoriesInBase(pageable);
        List<Category> categories = categoriessPage.getContent();

        assertEquals(2, categories.size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testFindUnallocatedCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        categoryService.addToCategory(1, 2);
        Page<Category> categoriesPage = categoryService.findUnallocatedCategories(pageable);
        List<Category> categories = categoriesPage.getContent();

        assertEquals(0, categories.size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testFindMainCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        categoryService.addToCategory(1, 2);
        Page<Category> categoriesPage = categoryService.findMainCategories(pageable);
        List<Category> categories = categoriesPage.getContent();

        assertEquals(1, categories.size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testAddToCategory() {
        categoryService.addToCategory(1, 2);
        long real = categoryService.getCategoryById(1).getParentCategory().getId();
        int size = categoryService.getCategoryById(2).getChildrenCategories().size();
        assertEquals(2, real);
        assertEquals(1, size);
    }

    @Sql("/testsss.sql")
    @Test
    public void testRemoveFromCategory() {
        categoryService.addToCategory(1, 2);
        categoryService.removeFromCategory(1);
        int real = categoryService.getCategoryById(2).getChildrenCategories().size();
        assertEquals(0, real);
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateGifLinkToCategory() {
        categoryService.updateGifLinkToCategory(1, "LINK_");

        Category updatedCategory = categoryService.getCategoryById(1);

        assertEquals("LINK_", updatedCategory.getGifLink());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateGifPreviewToCategory() {
        categoryService.updateGifPreviewToCategory(1, "pLINK_");

        Category updatedCategory = categoryService.getCategoryById(1);

        assertEquals("pLINK_", updatedCategory.getGifPreview());
    }
}
