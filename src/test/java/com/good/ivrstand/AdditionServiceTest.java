package com.good.ivrstand;

import com.good.ivrstand.app.AdditionService;
import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Addition;
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
public class AdditionServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private AdditionService additionService;

    @Sql("/testsss.sql")
    @Test
    public void testCreateAddition() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        Addition savedAddition = additionService.createAddition(addition);
        assertNotNull(savedAddition.getId());
        assertEquals(1, itemService.getItemById(1).getAdditions().size());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetAdditionById() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        Addition retrievedAddition = additionService.getAdditionById(1);
        assertNotNull(retrievedAddition);
        assertEquals(1, retrievedAddition.getId());
    }

    @Sql("/testsss.sql")
    @Test
    public void testGetAdditionByIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            additionService.getAdditionById(4);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testDeleteItem() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        additionService.deleteAddition(1);
        assertThrows(IllegalArgumentException.class, () -> {
            additionService.getAdditionById(1);
        });
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateTitleToAddition() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        additionService.updateTitleToAddition(1, "NT");

        Addition updatedAddition = additionService.getAdditionById(1);

        assertEquals("NT", updatedAddition.getTitle());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateDescriptionToAddition() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        additionService.updateDescriptionToAddition(1, "DEEEESC");

        Addition updatedAddition = additionService.getAdditionById(1);

        assertEquals("DEEEESC", updatedAddition.getDescription());
    }

    @Sql("/testsss.sql")
    @Test
    public void testUpdateGifLinkToAddition() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        additionService.updateGifLinkToAddition(1, "LINK_");

        Addition updatedAddition = additionService.getAdditionById(1);

        assertEquals("LINK_", updatedAddition.getGifLink());
    }

    @Sql("/testsss.sql")
    @Test
    public void testFindByItemId() {
        Addition addition = Addition.builder()
                .title("title")
                .description("test")
                .gifLink("link")
                .item(itemService.getItemById(1))
                .build();
        additionService.createAddition(addition);

        Pageable pageable = PageRequest.of(0, 10);
        long id = 1;
        Page<Addition> additionsPage = additionService.findByItemId(id, pageable);
        List<Addition> additions = additionsPage.getContent();

        assertEquals(1, additions.size());
    }
}
