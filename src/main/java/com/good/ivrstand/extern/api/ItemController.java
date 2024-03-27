package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemAssembler itemAssembler;

    @Autowired
    public ItemController(ItemService itemService, ItemAssembler itemAssembler) {
        this.itemService = itemService;
        this.itemAssembler = itemAssembler;
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@RequestBody ItemDTO itemDTO) {
        Item newItem = Item.builder()
                .title(itemDTO.getTitle())
                .description(itemDTO.getDescription())
                .gifLink(itemDTO.getGifLink())
                .build();

        itemService.createItem(newItem);

        return new ResponseEntity<>(itemAssembler.toModel(newItem), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(itemAssembler.toModel(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/categories/add/{categoryId}")
    public ResponseEntity<Void> addToCategory(@PathVariable long itemId, @PathVariable long categoryId) {
        itemService.addToCategory(itemId, categoryId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/categories/remove/{categoryId}")
    public ResponseEntity<Void> removeFromCategory(@PathVariable long itemId) {
        itemService.removeFromCategory(itemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<Item>> getAllItems(Pageable pageable) {
        Page<Item> items = itemService.getAllItemsInBase(pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Item>> findItemsByTitle(@RequestParam String title, Pageable pageable) {
        Page<Item> items = itemService.findItemsByTitle(title, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search/withoutCategory")
    public ResponseEntity<Page<Item>> findItemsWithoutCategory(Pageable pageable) {
        Page<Item> items = itemService.findItemsWithoutCategory(pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search/byCategoryAndTitle")
    public ResponseEntity<Page<Item>> findItemsByCategoryAndTitle(@RequestParam long categoryId, @RequestParam String title, Pageable pageable) {
        Page<Item> items = itemService.findItemsByTitleAndCategory(categoryId, title, pageable);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescriptionToItem(@PathVariable long id, @RequestBody String description) {
        itemService.updateDescriptionToItem(id, description);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/gif")
    public ResponseEntity<Void> updateItemGifLink(@PathVariable long id, @RequestBody String gifLink) {
        itemService.updateGifLinkToItem(id, gifLink);
        return ResponseEntity.ok().build();
    }
}
