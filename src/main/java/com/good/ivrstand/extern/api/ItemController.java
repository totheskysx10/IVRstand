package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Item;
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
@RequestMapping("/items")
@Tag(name = "ItemController", description = "Контроллер для управления услугами")
public class ItemController {

    private final ItemService itemService;
    private final ItemAssembler itemAssembler;

    @Autowired
    public ItemController(ItemService itemService, ItemAssembler itemAssembler) {
        this.itemService = itemService;
        this.itemAssembler = itemAssembler;
    }

    @Operation(summary = "Создать услугу", description = "Создает новую услугу.")
    @ApiResponse(responseCode = "201", description = "Услуга успешно создана")
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@RequestBody ItemDTO itemDTO) {
        Item newItem = Item.builder()
                .title(itemDTO.getTitle())
                .description(itemDTO.getDescription())
                .gifLink(itemDTO.getGifLink())
                .additions(new ArrayList<>())
                .build();

        itemService.createItem(newItem);

        return new ResponseEntity<>(itemAssembler.toModel(newItem), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить услугу по ID", description = "Получает информацию об услуге по ее идентификатору.")
    @ApiResponse(responseCode = "200", description = "Услуга найдена")
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(itemAssembler.toModel(item));
    }

    @Operation(summary = "Удалить услугу", description = "Удаляет услугу по ее идентификатору.")
    @ApiResponse(responseCode = "204", description = "Услуга успешно удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить в категорию", description = "Добавляет услугу в указанную категорию.")
    @ApiResponse(responseCode = "200", description = "Услуга успешно добавлена в категорию")
    @PutMapping("/{itemId}/category/add/{categoryId}")
    public ResponseEntity<Void> addToCategory(@PathVariable long itemId, @PathVariable long categoryId) {
        itemService.addToCategory(itemId, categoryId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить из категории", description = "Удаляет услугу из категории.")
    @ApiResponse(responseCode = "200", description = "Услуга успешно удалена из категории")
    @PutMapping("/{itemId}/category/remove")
    public ResponseEntity<Void> removeFromCategory(@PathVariable long itemId) {
        itemService.removeFromCategory(itemId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить все услуги", description = "Получает список всех услуг.")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<Page<ItemDTO>> getAllItems(Pageable pageable) {
        Page<ItemDTO> items = itemService.getAllItemsInBase(pageable).map(itemAssembler::toModel);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги по заголовку (заголовок можно ввести частично)", description = "Поиск услуг по заголовку (или его части).")
    @ApiResponse(responseCode = "200")
    @GetMapping("/search")
    public ResponseEntity<Page<ItemDTO>> findItemsByTitle(@RequestParam String title, Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsByTitle(title, pageable).map(itemAssembler::toModel);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги без категории", description = "Поиск услуг, которые не принадлежат ни одной категории.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/search/withoutCategory")
    public ResponseEntity<Page<ItemDTO>> findItemsWithoutCategory(Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsWithoutCategory(pageable).map(itemAssembler::toModel);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги по категории и заголовку (заголовок можно ввести частично)", description = "Поиск услуг по категории и заголовку (или его части).")
    @ApiResponse(responseCode = "200")
    @GetMapping("/search/byCategoryAndTitle")
    public ResponseEntity<Page<ItemDTO>> findItemsByCategoryAndTitle(@RequestParam long categoryId, @RequestParam String title, Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsByTitleAndCategory(categoryId, title, pageable).map(itemAssembler::toModel);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Найти услуги по категории", description = "Поиск услуг по категории.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/search/byCategory")
    public ResponseEntity<Page<ItemDTO>> findItemsByCategory(@RequestParam long categoryId, Pageable pageable) {
        Page<ItemDTO> items = itemService.findItemsByCategory(categoryId, pageable).map(itemAssembler::toModel);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Обновить описание услуги", description = "Обновляет описание услуги по ее идентификатору.")
    @ApiResponse(responseCode = "200", description = "Описание услуги успешно обновлено")
    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescriptionToItem(@PathVariable long id, @RequestBody String description) {
        itemService.updateDescriptionToItem(id, description);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить ссылку на GIF услуги", description = "Обновляет ссылку на GIF услуги по ее идентификатору.")
    @ApiResponse(responseCode = "200", description = "Ссылка на GIF услуги успешно обновлена")
    @PutMapping("/{id}/gif")
    public ResponseEntity<Void> updateItemGifLink(@PathVariable long id, @RequestBody String gifLink) {
        itemService.updateGifLinkToItem(id, gifLink);
        return ResponseEntity.ok().build();
    }
}
