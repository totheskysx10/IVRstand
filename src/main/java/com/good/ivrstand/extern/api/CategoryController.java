package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.CategoryService;
import com.good.ivrstand.domain.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/categories")
@Tag(name = "CategoryController", description = "Контроллер для управления категориями")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryAssembler categoryAssembler;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryAssembler categoryAssembler) {
        this.categoryService = categoryService;
        this.categoryAssembler = categoryAssembler;
    }

    @Operation(summary = "Создать категорию", description = "Создает новую категорию.")
    @ApiResponse(responseCode = "201", description = "Категория успешно создана")
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .title(categoryDTO.getTitle())
                .itemsInCategory(new ArrayList<>())
                .childrenCategories(new ArrayList<>())
                .gifLink(categoryDTO.getGifLink())
                .build();

        categoryService.createCategory(newCategory);

        return new ResponseEntity<>(categoryAssembler.toModel(newCategory), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить категорию по ID", description = "Получает информацию о категории по ее идентификатору.")
    @ApiResponse(responseCode = "200", description = "Категория найдена")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryAssembler.toModel(category));
    }

    @Operation(summary = "Удалить категорию", description = "Удаляет категорию по ее идентификатору.")
    @ApiResponse(responseCode = "204", description = "Категория успешно удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все категории", description = "Получает список всех категорий.")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getAllCategoriesInBase(pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Найти категории по заголовку (заголовок можно ввести частично)", description = "Поиск категорий по заголовку (или его части).")
    @ApiResponse(responseCode = "200", description = "Категории найдены")
    @GetMapping("/search")
    public ResponseEntity<Page<CategoryDTO>> findCategoriesByTitle(@RequestParam String title, Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.findCategoriesByTitle(title, pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Найти нераспределенные категории", description = "Поиск категорий, которые не принадлежат ни одной другой категории.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/search/unallocated")
    public ResponseEntity<Page<CategoryDTO>> findUnallocatedCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.findUnallocatedCategories(pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Установить родительскую категорию", description = "Устанавливает указанную категорию в качестве родительской для другой категории.")
    @ApiResponse(responseCode = "200", description = "Родительская категория успешно установлена")
    @PutMapping("/{categoryId}/parent/set/{parentId}")
    public ResponseEntity<Void> addToCategory(@PathVariable long categoryId, @PathVariable long parentId) {
        categoryService.addToCategory(categoryId, parentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить дочернюю категорию из родительской категории", description = "Удаляет указанную категорию из списка дочерних категорий родительской категории.")
    @ApiResponse(responseCode = "200", description = "Подкатегория успешно удалена из категории")
    @PutMapping("/{categoryId}/children/remove/{childId}")
    public ResponseEntity<Void> removeFromCategory(@PathVariable long childId) {
        categoryService.removeFromCategory(childId);
        return ResponseEntity.ok().build();
    }
}