package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.CategoryService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryAssembler categoryAssembler;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryAssembler categoryAssembler) {
        this.categoryService = categoryService;
        this.categoryAssembler = categoryAssembler;
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .title(categoryDTO.getTitle())
                .itemsInCategory(new ArrayList<>())
                .build();

        categoryService.createCategory(newCategory);

        return new ResponseEntity<>(categoryAssembler.toModel(newCategory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryAssembler.toModel(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getAllCategoriesInBase(pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryDTO>> findCategoriesByTitle(@RequestParam String title, Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.findCategoriesByTitle(title, pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }
}
