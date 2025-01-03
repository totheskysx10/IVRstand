package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.app.service.EncodeService;
import com.good.ivrstand.app.service.ItemService;
import com.good.ivrstand.domain.Item;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.ItemCategoryAddDeleteException;
import com.good.ivrstand.exception.ItemUpdateException;
import com.good.ivrstand.exception.ItemsFindException;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import com.good.ivrstand.exception.notfound.ItemNotFoundException;
import com.good.ivrstand.extern.api.assembler.ItemAssembler;
import com.good.ivrstand.extern.api.dto.DescriptionUpdateDTO;
import com.good.ivrstand.extern.api.dto.ItemDTO;
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
import java.util.Collections;

@RestController
@RequestMapping("/items")
@Tag(name = "ItemController", description = "Контроллер для управления услугами")
public class ItemController {

    private final ItemService itemService;
    private final ItemAssembler itemAssembler;
    private final EncodeService encodeService;

    @Autowired
    public ItemController(ItemService itemService, ItemAssembler itemAssembler, EncodeService encodeService) {
        this.itemService = itemService;
        this.itemAssembler = itemAssembler;
        this.encodeService = encodeService;
    }

    @Operation(summary = "Создать услугу", description = "Создает новую услугу. Если включить флаг enableAudio, сгенерируется речь для для заголовка и описания.")
    @ApiResponse(responseCode = "201", description = "Услуга успешно создана")
    @ApiResponse(responseCode = "409", description = "Ошибка валидации")
    @Transactional
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@RequestBody @Valid ItemDTO itemDTO) {
        try {
            Item newItem = Item.builder()
                    .title(itemDTO.getTitle())
                    .description(itemDTO.getDescription())
                    .gifPreview(itemDTO.getGifPreview())
                    .gifLink(itemDTO.getGifLink())
                    .additions(new ArrayList<>())
                    .iconLinks(new ArrayList<>())
                    .keywords(new ArrayList<>())
                    .mainIconLink(itemDTO.getMainIconLink())
                    .audio(new ArrayList<>())
                    .descriptionHash(encodeService.generateHashForAudio(itemDTO.getDescription()))
                    .build();

            itemService.createItem(newItem, itemDTO.isEnableAudio());

            return new ResponseEntity<>(itemAssembler.toModel(newItem), HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Получить услугу по ID", description = "Получает информацию об услуге по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуга найдена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable long id) {
        try {
            Item item = itemService.getItemById(id);
            return ResponseEntity.ok(itemAssembler.toModel(item));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить услугу", description = "Удаляет услугу по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуга успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Добавить в категорию", description = "Добавляет услугу в указанную категорию.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуга успешно добавлена в категорию"),
            @ApiResponse(responseCode = "409", description = "Услуга уже в категории или категория не конечная"),
            @ApiResponse(responseCode = "404", description = "Услуга/категория не найдена")
    })
    @Transactional
    @PutMapping("/{itemId}/category/add/{categoryId}")
    public ResponseEntity<Void> addToCategory(@PathVariable long itemId, @PathVariable long categoryId) {
        try {
            itemService.addToCategory(itemId, categoryId);
            return ResponseEntity.ok().build();
        } catch (ItemCategoryAddDeleteException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (ItemNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить из категории", description = "Удаляет услугу из категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Услуга успешно удалена из категории"),
            @ApiResponse(responseCode = "409", description = "Услуга не лежит ни в одной из категорий"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @Transactional
    @PutMapping("/{itemId}/category/remove")
    public ResponseEntity<Void> removeFromCategory(@PathVariable long itemId) {
        try {
            itemService.removeFromCategory(itemId);
            return ResponseEntity.ok().build();
        } catch (ItemCategoryAddDeleteException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получить все услуги", description = "Получает список всех услуг.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable) {
        Page<ItemDTO> items = itemService.getAllItemsInBase(pageable).map(itemAssembler::toModel);

        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги по заголовку (заголовок можно ввести частично)", description = "Поиск услуг по заголовку (или его части).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат"),
            @ApiResponse(responseCode = "500", description = "Ошибка синхронизации при поиске услуг"),
            @ApiResponse(responseCode = "404", description = "Ошибка приложения при поиске")
    })
    @GetMapping("/search")
    public ResponseEntity<Object> findItemsByTitle(@RequestParam String title, Pageable pageable) {
        try {
            Page<ItemDTO> items = itemService.findItemsByTitle(title, pageable).map(itemAssembler::toModel);
            if (items.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(items);
        } catch (ItemsFindException e) {
            String errorMessage = "Ошибка при поиске услуг: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", errorMessage));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Найти услуги без категории", description = "Поиск услуг, которые не принадлежат ни одной категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping("/search/withoutCategory")
    public ResponseEntity<Page<ItemDTO>> findItemsWithoutCategory(Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsWithoutCategory(pageable).map(itemAssembler::toModel);

        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги по категории", description = "Поиск услуг по категории.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping("/search/byCategory")
    public ResponseEntity<Page<ItemDTO>> findItemsByCategory(@RequestParam long categoryId, Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsByCategory(categoryId, pageable).map(itemAssembler::toModel);

        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Обновить описание услуги", description = "Обновляет описание услуги по ее идентификатору. Если включить флаг enableAudio, сгенерируется речь описания, иначе - удалится (если есть) или не будет сгененрирована.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Описание услуги успешно обновлено"),
            @ApiResponse(responseCode = "409", description = "Дубликат файла аудио по названию"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescriptionToItem(
            @PathVariable long id,
            @RequestBody DescriptionUpdateDTO descriptionUpdateDTO) throws IOException {

        String description = descriptionUpdateDTO.getDescription();
        boolean enableAudio = descriptionUpdateDTO.isEnableAudio();

        try {
            itemService.updateDescriptionToItem(id, description, enableAudio);
            return ResponseEntity.ok().build();
        } catch (FileDuplicateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на GIF-превью услуги", description = "Обновляет ссылку на GIF-превью услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка на GIF-превью услуги успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/gif-preview")
    public ResponseEntity<Void> updateItemGifPreview(@PathVariable long id, @RequestBody String gifPreview) {
        try {
            itemService.updateGifPreviewToItem(id, gifPreview);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на GIF услуги", description = "Обновляет ссылку на GIF услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка на GIF услуги успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/gif")
    public ResponseEntity<Void> updateItemGifLink(@PathVariable long id, @RequestBody String gifLink) {
        try {
            itemService.updateGifLinkToItem(id, gifLink);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить ссылку на главную иконку услуги", description = "Обновляет ссылку на главную иконку услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка на главную иконку услуги успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/main-icon")
    public ResponseEntity<Void> updateItemMainIcon(@PathVariable long id, @RequestBody String link) {
        try {
            itemService.updateMainIconToItem(id, link);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Добавить иконку услуги", description = "Добавляет иконку для услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Иконка для услуги успешно добавлена"),
            @ApiResponse(responseCode = "409", description = "У услуги уже есть эта иконка"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/icon/add")
    public ResponseEntity<Void> addItemIcon(@PathVariable long id, @RequestBody String iconLink) {
        try {
            itemService.addIcon(id, iconLink);
            return ResponseEntity.ok().build();
        } catch (ItemUpdateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить иконку услуги", description = "Удаляет иконку для услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Иконка для услуги успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/icon/remove")
    public ResponseEntity<Void> removeItemIcon(@PathVariable long id, @RequestBody String iconLink) {
        try {
            itemService.removeIcon(id, iconLink);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Очистить иконки услуги", description = "Очищает иконки услуги по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Иконки услуги очищены"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/clear-icons")
    public ResponseEntity<Void> clearIcons(@PathVariable long id) {
        try {
            itemService.clearIcons(id);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Добавить ключевое слово услуги", description = "Добавляет ключевое слово для услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ключевое слово для услуги успешно добавлено"),
            @ApiResponse(responseCode = "409", description = "У услуги уже есть это ключевое слово"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/keyword/add")
    public ResponseEntity<Void> addKeyword(@PathVariable long id, @RequestBody String word) {
        try {
            itemService.addKeyword(id, word);
            return ResponseEntity.ok().build();
        } catch (ItemUpdateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить ключевое слово услуги", description = "Удаляет ключевое слово для услуги по ее идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ключевое слово для услуги успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/keyword/remove")
    public ResponseEntity<Void> removeKeyword(@PathVariable long id, @RequestBody String word) {
        try {
            itemService.removeKeyword(id, word);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Очистить ключевые слова услуги", description = "Очищает ключевые слова услуги по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ключевые слова услуги очищены"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/clear-keywords")
    public ResponseEntity<Void> clearKeywords(@PathVariable long id) {
        try {
            itemService.clearKeywords(id);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Сгенерировать аудио заголовка услуги", description = "Генерирует аудио заголовка услуги по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аудио заголовка услуги готово"),
            @ApiResponse(responseCode = "409", description = "У услуги уже есть аудио заголовка. Иногда (читать как никогда) падает при дубликате заголовка аудио"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/title-audio/generate")
    public ResponseEntity<Void> generateTitleAudio(@PathVariable long id) throws IOException {
        try {
            itemService.generateTitleAudio(id);
            return ResponseEntity.ok().build();
        } catch (FileDuplicateException | ItemUpdateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить аудио заголовка услуги", description = "Удаляет аудио заголовка услуги по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аудио заголовка услуги удалено"),
            @ApiResponse(responseCode = "409", description = "У услуги уже нет аудио заголовка"),
            @ApiResponse(responseCode = "404", description = "Услуга не найдена")
    })
    @PutMapping("/{id}/title-audio/remove")
    public ResponseEntity<Void> removeTitleAudio(@PathVariable long id) {
        try {
            itemService.removeTitleAudio(id);
            return ResponseEntity.ok().build();
        } catch (ItemUpdateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
