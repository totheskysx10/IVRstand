package com.good.ivrstand;

import com.good.ivrstand.app.service.CategoryService;
import com.good.ivrstand.app.service.ItemService;
import com.good.ivrstand.domain.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testCreateItem() {
        Item item = new Item();
        item.setTitle("TestTitle");
        item.setDescription("TestDesc");
        item.setGifLink("TestLink");
        Item savedItem = itemService.createItem(item, false);
        assertNotNull(savedItem.getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetItemById() {
        Item retrievedItem = itemService.getItemById(1);
        assertNotNull(retrievedItem);
        assertEquals(1, retrievedItem.getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetItemByIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.getItemById(4);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testDeleteItem() {
        itemService.deleteItem(1);
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.getItemById(1);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testAddToCategory() {
        itemService.addToCategory(1, 1);
        int real = categoryService.getCategoryById(1).getItemsInCategory().size();
        assertEquals(1, real);
    }

    @Sql("/testsss.sql")
    @Test
    public void testRemoveFromCategory() {
        itemService.addToCategory(1, 1);
        itemService.removeFromCategory(1);
        int real = categoryService.getCategoryById(1).getItemsInCategory().size();
        assertEquals(0, real);
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetAllItemsInBase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemsPage = itemService.getAllItemsInBase(pageable);
        List<Item> items = itemsPage.getContent();

        assertEquals(3, items.size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testFindItemsByTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        String request = "testItem";
        Page<Item> itemsPage = itemService.findItemsByTitle(request, pageable, 0);
        List<Item> items = itemsPage.getContent();

        assertEquals(3, items.size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testFindItemsWithoutCategory() {
        itemService.addToCategory(1, 1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemsPage = itemService.findItemsWithoutCategory(pageable);
        List<Item> items = itemsPage.getContent();

        assertEquals(2, items.get(0).getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateDescriptionToItem() throws IOException {
        itemService.updateDescriptionToItem(1, "DEEEESC", false);

        Item updatedItem = itemService.getItemById(1);

        assertEquals("DEEEESC", updatedItem.getDescription());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateGifLinkToItem() {
        itemService.updateGifLinkToItem(1, "LINK_");

        Item updatedItem = itemService.getItemById(1);

        assertEquals("LINK_", updatedItem.getGifLink());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateGifPreviewToItem() {
        itemService.updateGifPreviewToItem(1, "pLINK_");

        Item updatedItem = itemService.getItemById(1);

        assertEquals("pLINK_", updatedItem.getGifPreview());
    }
}
