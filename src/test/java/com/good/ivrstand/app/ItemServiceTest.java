package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.ItemRepository;
import com.good.ivrstand.app.service.*;
import com.good.ivrstand.app.service.externinterfaces.FlaskApiVectorSearchService;
import com.good.ivrstand.domain.*;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.ItemCategoryAddDeleteException;
import com.good.ivrstand.exception.ItemUpdateException;
import com.good.ivrstand.exception.notfound.CategoryNotFoundException;
import com.good.ivrstand.exception.notfound.ItemNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private FlaskApiVectorSearchService flaskApiVectorSearchService;

    @Mock
    private SpeechService speechService;

    @Mock
    private EncodeService encodeService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void testCreateItem() throws Exception {
        Item item = new Item(1L,
                            "title",
                            "desc",
                            "preview",
                            "gif",
                            null,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            "mainIcon",
                            new ArrayList<>(),
                            new ArrayList<>(),
                            "titleAuido",
                            "hash");

        when(itemRepository.save(item)).thenReturn(item);
        when(itemRepository.findByHashAndAudioExistence("hash", PageRequest.of(0, 1)))
                .thenReturn(Page.empty());
        when(speechService.splitDescription("desc")).thenReturn(new String[]{"desc"});

        Item createdItem = itemService.createItem(item, true);

        assertNotNull(createdItem);
        assertEquals("title", createdItem.getTitle());
        verify(itemRepository).save(item);
        verify(speechService).generateAudio("title");
        verify(flaskApiVectorSearchService).addTitle(argThat(
                addTitleRequest -> addTitleRequest.getText().equals("title  desc")));
    }

    @Test
    void testGetItemById() throws ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Item foundItem = itemService.getItemById(1L);

        assertNotNull(foundItem);
        assertEquals("title", foundItem.getTitle());
    }

    @Test
    void testGetItemByIdNotFound() {
        when(itemRepository.findById(1L)).thenReturn(null);

        Exception e = assertThrows(ItemNotFoundException.class, () -> {
            itemService.getItemById(1L);
        });

        assertEquals("Услуга с id 1 не найдена", e.getMessage());
    }

    @Test
    void testDeleteItem() throws ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.deleteItem(1L);

        verify(itemRepository).deleteById(1L);
        verify(flaskApiVectorSearchService).deleteTitle(argThat(
                titleRequest -> titleRequest.getText().equals("title  desc")));
    }

    @Test
    void testAddToCategory() throws ItemCategoryAddDeleteException, CategoryNotFoundException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        Category category = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        when(itemRepository.findById(1L)).thenReturn(item);
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        itemService.addToCategory(1L, 1L);

        assertEquals(1L, item.getCategory().getId());
        verify(itemRepository).save(item);
        verify(flaskApiVectorSearchService).deleteTitle(argThat(
                titleRequest -> titleRequest.getText().equals("title  desc")));
        verify(flaskApiVectorSearchService).addTitle(argThat(
                addTitleRequest -> addTitleRequest.getText().equals("title  ctitle desc")));
    }

    @Test
    void testAddToCategoryAlreadyHasCategory() throws CategoryNotFoundException {
        Category category = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                category,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        Exception exception = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            itemService.addToCategory(1L, 1L);
        });

        assertEquals("Услуга с id 1 уже в другой категории!", exception.getMessage());
    }

    @Test
    void testAddToCategoryNotFinal() throws CategoryNotFoundException {
        Category category1 = new Category(2L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");
        
        Category category2 = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                List.of(category1),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);
        when(categoryService.getCategoryById(1L)).thenReturn(category1);
        when(categoryService.getCategoryById(2L)).thenReturn(category2);

        Exception e = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            itemService.addToCategory(1L, 2L);
        });

        assertEquals("В категории с id 2 есть подкатегории - услугу можно добавить только в конечную подкатегорию!", e.getMessage());
    }

    @Test
    void testRemoveFromCategory() throws ItemCategoryAddDeleteException, ItemNotFoundException {
        Category category = new Category(1L,
                "ctitle",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "preview",
                "link",
                "icon",
                "audio");

        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                category,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.removeFromCategory(1L);

        assertNull(item.getCategory());
        verify(itemRepository).save(item);
        verify(flaskApiVectorSearchService).deleteTitle(argThat(
                titleRequest -> titleRequest.getText().equals("title  ctitle desc")));
        verify(flaskApiVectorSearchService).addTitle(argThat(
                addTitleRequest -> addTitleRequest.getText().equals("title  desc")));
    }

    @Test
    void testRemoveFromCategoryNotInCategory() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Exception e = assertThrows(ItemCategoryAddDeleteException.class, () -> {
            itemService.removeFromCategory(1L);
        });

        assertEquals("Услуга с id 1 не относится ни к одной из категорий!", e.getMessage());
    }

    @Test
    void testUpdateDescriptionToItem() throws IOException, FileDuplicateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        String newDescription = "New Description";

        when(itemRepository.findById(1L)).thenReturn(item);
        when(encodeService.generateHashForAudio("New Description")).thenReturn("newHash");
        when(itemRepository.findByHashAndAudioExistence("newHash", PageRequest.of(0, 1)))
                .thenReturn(Page.empty());
        when(speechService.splitDescription("New Description")).thenReturn(new String[]{"New Description"});

        itemService.updateDescriptionToItem(1L, newDescription, true);

        assertEquals("New Description", item.getDescription());
        assertEquals("newHash", item.getDescriptionHash());
        verify(itemRepository).save(item);
        verify(speechService).generateAudio("New Description");
        verify(flaskApiVectorSearchService).deleteTitle(argThat(
                titleRequest -> titleRequest.getText().equals("title  desc")));
        verify(flaskApiVectorSearchService).addTitle(argThat(
                addTitleRequest -> addTitleRequest.getText().equals("title  New Description")));
    }

    @Test
    void testAddKeyword() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addKeyword(1L, "TestKeyword");

        assertTrue(item.getKeywords().contains("TestKeyword"));
        verify(itemRepository).save(item);
    }

    @Test
    void testAddKeywordAlreadyHas() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                List.of("TestKeyword"),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Exception e = assertThrows(ItemUpdateException.class, () -> {
            itemService.addKeyword(1L, "TestKeyword");
        });

        assertEquals("Ключевое слово для услуги с id 1 уже было добавлено раннее!", e.getMessage());
    }

    @Test
    void testRemoveKeyword() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addKeyword(1L, "TestKeyword");
        itemService.removeKeyword(1L, "TestKeyword");

        assertTrue(item.getKeywords().isEmpty());
        verify(itemRepository, times(2)).save(item);
    }

    @Test
    void testClearKeywords() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addKeyword(1L, "TestKeyword");
        itemService.clearKeywords(1L);

        assertTrue(item.getKeywords().isEmpty());
        verify(itemRepository, times(2)).save(item);
    }

    @Test
    void testAddIcon() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addIcon(1L, "icon");

        assertTrue(item.getIconLinks().contains("icon"));
        verify(itemRepository).save(item);
    }

    @Test
    void testAddIconHas() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                List.of("icon"),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Exception e = assertThrows(ItemUpdateException.class, () -> {
            itemService.addIcon(1L, "icon");
        });

        assertEquals("Иконка для услуги с id 1 уже была добавлена раннее!", e.getMessage());
    }

    @Test
    void testRemoveIcon() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addIcon(1L, "icon");
        itemService.removeIcon(1L, "icon");

        assertTrue(item.getIconLinks().isEmpty());
        verify(itemRepository, times(2)).save(item);
    }

    @Test
    void testClearIcons() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.addIcon(1L, "icon");
        itemService.clearIcons(1L);

        assertTrue(item.getIconLinks().isEmpty());
        verify(itemRepository, times(2)).save(item);
    }

    @Test
    void testUpdateGifLinkToItem() throws ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.updateGifLinkToItem(1L, "newGifLink");

        assertEquals("newGifLink", item.getGifLink());
        verify(itemRepository).save(item);
    }

    @Test
    void testUpdateGifPreviewToItem() throws ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.updateGifPreviewToItem(1L, "newGifLink");

        assertEquals("newGifLink", item.getGifPreview());
        verify(itemRepository).save(item);
    }

    @Test
    void testUpdateMainIconToItem() throws ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "titleAuido",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.updateMainIconToItem(1L, "newMainIcon");

        assertEquals("newMainIcon", item.getMainIconLink());
        verify(itemRepository).save(item);
    }

    @Test
    void generateTitleAudio() throws IOException, ItemUpdateException, FileDuplicateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);
        when(speechService.generateAudio("title")).thenReturn("audio");

        itemService.generateTitleAudio(1L);

        assertEquals("audio", item.getTitleAudio());
        verify(itemRepository).save(item);
    }

    @Test
    void generateTitleAudioAlreadyHas() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "audio",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Exception e = assertThrows(ItemUpdateException.class, () -> {
            itemService.generateTitleAudio(1L);
        });

        assertEquals("У услуги 1 уже есть аудио заголовка!", e.getMessage());
    }

    @Test
    void removeTitleAudio() throws ItemUpdateException, ItemNotFoundException {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                "audio",
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        itemService.removeTitleAudio(1L);

        assertNull(item.getTitleAudio());
        verify(itemRepository).save(item);
    }

    @Test
    void removeTitleAudioNoAudio() {
        Item item = new Item(1L,
                "title",
                "desc",
                "preview",
                "gif",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                "mainIcon",
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                "hash");

        when(itemRepository.findById(1L)).thenReturn(item);

        Exception e = assertThrows(ItemUpdateException.class, () -> {
            itemService.removeTitleAudio(1L);
        });

        assertEquals("У услуги 1 нет аудио заголовка!", e.getMessage());
    }
}
