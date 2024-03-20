package com.good.ivrstand;

import com.good.ivrstand.app.CategoryService;
import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Category;
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
        Category savedCategory = categoryService.createCategory(category);
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
    public void testFindCategoriesByTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        String request = "Category";
        Page<Category> categoriessPage = categoryService.findCategoriesByTitle(request, pageable);
        List<Category> categories = categoriessPage.getContent();

        assertEquals(2, categories.size());
    }
}
