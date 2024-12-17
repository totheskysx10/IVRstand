package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.AdditionRepository;
import com.good.ivrstand.app.service.*;
import com.good.ivrstand.domain.*;
import com.good.ivrstand.exception.AdditionUpdateException;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.exception.notfound.AdditionNotFoundException;
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
class AdditionServiceTest {

    @Mock
    private AdditionRepository additionRepository;

    @InjectMocks
    private AdditionService additionService;

    @Mock
    private SpeechService speechService;

    @Mock
    private EncodeService encodeService;

    @Test
    void testCreateAddition() throws Exception {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.save(addition)).thenReturn(addition);
        when(additionRepository.findByHashAndAudioExistence("hash", PageRequest.of(0, 1)))
                .thenReturn(Page.empty());
        when(speechService.splitDescription("desc")).thenReturn(new String[]{"desc"});

        Addition createdAddition = additionService.createAddition(addition, true);

        assertNotNull(createdAddition);
        assertEquals("title", createdAddition.getTitle());
        verify(additionRepository).save(addition);
        verify(speechService).generateAudio("title");
    }

    @Test
    void testGetAdditionById() throws AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        Addition foundAddition = additionService.getAdditionById(1L);

        assertNotNull(foundAddition);
        assertEquals("title", foundAddition.getTitle());
    }

    @Test
    void testGetAdditionByIdNotFound() {
        when(additionRepository.findById(1L)).thenReturn(null);

        Exception e = assertThrows(AdditionNotFoundException.class, () -> {
            additionService.getAdditionById(1L);
        });

        assertEquals("Дополнение с id 1 не найдено", e.getMessage());
    }

    @Test
    void testDeleteAddition() {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.deleteAddition(1L);

        verify(additionRepository).deleteById(1L);
    }

    @Test
    void testUpdateDescriptionToItem() throws IOException, FileDuplicateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        String newDescription = "New Description";

        when(additionRepository.findById(1L)).thenReturn(addition);
        when(encodeService.generateHashForAudio("New Description")).thenReturn("newHash");
        when(additionRepository.findByHashAndAudioExistence("newHash", PageRequest.of(0, 1)))
                .thenReturn(Page.empty());
        when(speechService.splitDescription("New Description")).thenReturn(new String[]{"New Description"});

        additionService.updateDescriptionToAddition(1L, newDescription, true);

        assertEquals("New Description", addition.getDescription());
        assertEquals("newHash", addition.getDescriptionHash());
        verify(additionRepository).save(addition);
        verify(speechService).generateAudio("New Description");
    }

    @Test
    void testAddIcon() throws AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.addIcon(1L, "icon");

        assertTrue(addition.getIconLinks().contains("icon"));
        verify(additionRepository).save(addition);
    }

    @Test
    void testAddIconHas() {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                List.of("icon"),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        Exception e = assertThrows(AdditionUpdateException.class, () -> {
            additionService.addIcon(1L, "icon");
        });

        assertEquals("Иконка для дополнения с id 1 уже была добавлена раннее!", e.getMessage());
    }

    @Test
    void testRemoveIcon() throws AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.addIcon(1L, "icon");
        additionService.removeIcon(1L, "icon");

        assertTrue(addition.getIconLinks().isEmpty());
        verify(additionRepository, times(2)).save(addition);
    }

    @Test
    void testClearIcons() throws AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.addIcon(1L, "icon");
        additionService.clearIcons(1L);

        assertTrue(addition.getIconLinks().isEmpty());
        verify(additionRepository, times(2)).save(addition);
    }

    @Test
    void testUpdateGifLinkToAddition() throws AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.updateGifLinkToAddition(1L, "newGifLink");

        assertEquals("newGifLink", addition.getGifLink());
        verify(additionRepository).save(addition);
    }

    @Test
    void testUpdateGifPreviewToAddition() throws AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.updateGifPreviewToAddition(1L, "newGifLink");

        assertEquals("newGifLink", addition.getGifPreview());
        verify(additionRepository).save(addition);
    }

    @Test
    void testUpdateMainIconToAddition() throws AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.updateMainIconToAddition(1L, "newMainIcon");

        assertEquals("newMainIcon", addition.getMainIconLink());
        verify(additionRepository).save(addition);
    }

    @Test
    void testUpdateTitleToAddition() throws FileDuplicateException, IOException, AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        when(speechService.generateAudio("nt")).thenReturn("na");
        additionService.updateTitleToAddition(1L, "nt");

        assertEquals("nt", addition.getTitle());
        assertEquals("na", addition.getTitleAudio());
        verify(additionRepository, times(3)).save(addition);
    }

    @Test
    void testUpdateTitleToAdditionNoAudio() throws FileDuplicateException, IOException, AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                null,
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.updateTitleToAddition(1L, "nt");

        assertEquals("nt", addition.getTitle());
        assertEquals(null, addition.getTitleAudio());
        verify(additionRepository).save(addition);
    }

    @Test
    void generateTitleAudio() throws IOException, FileDuplicateException, AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                null,
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);
        when(speechService.generateAudio("title")).thenReturn("audio");

        additionService.generateTitleAudio(1L);

        assertEquals("audio", addition.getTitleAudio());
        verify(additionRepository).save(addition);
    }

    @Test
    void generateTitleAudioAlreadyHas() {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        Exception e = assertThrows(AdditionUpdateException.class, () -> {
            additionService.generateTitleAudio(1L);
        });

        assertEquals("У дополнения 1 уже есть аудио заголовка!", e.getMessage());
    }

    @Test
    void removeTitleAudio() throws AdditionUpdateException, AdditionNotFoundException {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                "audio",
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        additionService.removeTitleAudio(1L);

        assertNull(addition.getTitleAudio());
        verify(additionRepository).save(addition);
    }

    @Test
    void removeTitleAudioNoAudio() {
        Item item = new Item();
        Addition addition = new Addition(1L,
                "title",
                "desc",
                "preview",
                "gif",
                item,
                new ArrayList<>(),
                "icon",
                new ArrayList<>(),
                null,
                "hash");

        when(additionRepository.findById(1L)).thenReturn(addition);

        Exception e = assertThrows(AdditionUpdateException.class, () -> {
            additionService.removeTitleAudio(1L);
        });

        assertEquals("У дополнения 1 нет аудио заголовка!", e.getMessage());
    }
}
