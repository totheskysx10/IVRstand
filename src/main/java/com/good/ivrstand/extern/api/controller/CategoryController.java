package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.app.service.CategoryService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.exception.CategoryUpdateException;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.ItemCategoryAddDeleteException;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import com.good.ivrstand.extern.api.assembler.CategoryAssembler;
import com.good.ivrstand.extern.api.dto.CategoryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @Operation(summary = "Создать категорию", description = "Создает новую категорию. Если включить флаг enableAudio, сгенерируется речь для для заголовка.")
    @ApiResponse(responseCode = "201", description = "Категория успешно создана")
    @Transactional
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .title(categoryDTO.getTitle())
                .itemsInCategory(new ArrayList<>())
                .childrenCategories(new ArrayList<>())
                .gifPreview(categoryDTO.getGifPreview())
                .gifLink(categoryDTO.getGifLink())
                .mainIconLink(categoryDTO.getMainIconLink())
                .build();

        categoryService.createCategory(newCategory, categoryDTO.isEnableAudio());

        return new ResponseEntity<>(categoryAssembler.toModel(newCategory), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить категорию по ID", description = "Получает информацию о категории по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(categoryAssembler.toModel(category));
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Удалить категорию", description = "Удаляет категорию по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получить все категории", description = "Получает список всех категорий.")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.getAllCategoriesInBase(pageable).map(categoryAssembler::toModel);

        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Найти нераспределенные категории", description = "Поиск категорий, которые не принадлежат ни одной другой категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping("/search/unallocated")
    public ResponseEntity<Page<CategoryDTO>> findUnallocatedCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.findUnallocatedCategories(pageable).map(categoryAssembler::toModel);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Найти главные категории", description = "Поиск категорий, которые лежат в главном меню.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping("/search/main")
    public ResponseEntity<Page<CategoryDTO>> findMainCategories(Pageable pageable) {
        Page<CategoryDTO> categories = categoryService.findMainCategories(pageable).map(categoryAssembler::toModel);

        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Установить родительскую категорию", description = "Устанавливает указанную категорию в качестве родительской для другой категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Родительская категория успешно установлена"),
            @ApiResponse(responseCode = "409", description = "Подкатегория уже в категории или категория, в которую добавляют, имеет услуги"),
            @ApiResponse(responseCode = "404", description = "Категория/подкатегория не найдена")
    })
    @Transactional
    @PutMapping("/{categoryId}/parent/set/{parentId}")
    public ResponseEntity<Void> addToCategory(@PathVariable long categoryId, @PathVariable long parentId) {
        try {
            categoryService.addToCategory(categoryId, parentId);
            return ResponseEntity.ok().build();
        } catch (ItemCategoryAddDeleteException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить дочернюю категорию из родительской категории", description = "Удаляет указанную категорию из списка дочерних категорий родительской категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подкатегория успешно удалена из категории"),
            @ApiResponse(responseCode = "409", description = "Категория не лежит ни в одной из категорий"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @Transactional
    @PutMapping("/children/remove/{childId}")
    public ResponseEntity<Void> removeFromCategory(@PathVariable long childId) {
        try {
            categoryService.removeFromCategory(childId);
            return ResponseEntity.ok().build();
        } catch (ItemCategoryAddDeleteException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на GIF-превью категории", description = "Обновляет ссылку на GIF-превью категории по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка на GIF-превью категории успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @PutMapping("/{id}/gif-preview")
    public ResponseEntity<Void> updateCategoryGifPreview(@PathVariable long id, @RequestBody String gifPreview) {
        try {
            categoryService.updateGifPreviewToCategory(id, gifPreview);
            return ResponseEntity.ok().build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на GIF категории", description = "Обновляет ссылку на GIF категории по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка на GIF категории успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @PutMapping("/{id}/gif")
    public ResponseEntity<Void> updateCategoryGifLink(@PathVariable long id, @RequestBody String gifLink) {
        try {
            categoryService.updateGifLinkToCategory(id, gifLink);
            return ResponseEntity.ok().build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на главную иконку категории", description = "Обновляет ссылку на главную иконку категории по ее идентификатору.")
    @ApiResponse(responseCode = "200", description = "Ссылка на главную иконку категории успешно обновлена")
    @PutMapping("/{id}/main-icon")
    public ResponseEntity<Void> updateCategoryMainIcon(@PathVariable long id, @RequestBody String link) {
        try {
            categoryService.updateMainIconToCategory(id, link);
            return ResponseEntity.ok().build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Сгенерировать аудио заголовка категории", description = "Генерирует аудио заголовка категории по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аудио заголовка категории готово"),
            @ApiResponse(responseCode = "409", description = "У категории уже есть аудио заголовка. Иногда (читать как никогда) падает при дубликате заголовка аудио"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @PutMapping("/{id}/title-audio/generate")
    public ResponseEntity<Void> generateTitleAudio(@PathVariable long id) throws IOException {
        try {
            categoryService.generateTitleAudio(id);
            return ResponseEntity.ok().build();
        } catch (FileDuplicateException | CategoryUpdateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить аудио заголовка категории", description = "Удаляет аудио заголовка категории по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аудио заголовка категории удалено"),
            @ApiResponse(responseCode = "409", description = "У категории уже нет аудио заголовка"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @PutMapping("/{id}/title-audio/remove")
    public ResponseEntity<Void> removeTitleAudio(@PathVariable long id) {
        try {
            categoryService.removeTitleAudio(id);
            return ResponseEntity.ok().build();
        } catch (CategoryUpdateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
