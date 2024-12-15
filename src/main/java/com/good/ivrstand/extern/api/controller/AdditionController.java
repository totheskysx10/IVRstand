package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.app.service.AdditionService;
import com.good.ivrstand.app.service.EncodeService;
import com.good.ivrstand.app.service.ItemService;
import com.good.ivrstand.domain.Addition;
import com.good.ivrstand.exception.FileDuplicateException;
import com.good.ivrstand.extern.api.assembler.AdditionAssembler;
import com.good.ivrstand.extern.api.dto.AdditionDTO;
import com.good.ivrstand.extern.api.dto.DescriptionUpdateDTO;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping("/additions")
@Tag(name = "AdditionController", description = "Контроллер для управления дополнениями к услугам")
public class AdditionController {

    private final AdditionService additionService;
    private final AdditionAssembler additionAssembler;
    private final ItemService itemService;
    private final EncodeService encodeService;

    @Autowired
    public AdditionController(AdditionService additionService, AdditionAssembler additionAssembler, ItemService itemService, EncodeService encodeService) {
        this.additionService = additionService;
        this.additionAssembler = additionAssembler;
        this.itemService = itemService;
        this.encodeService = encodeService;
    }

    @Operation(summary = "Создать дополнение", description = "Создает новое дополнение для указанной услуги. Если включить флаг enableAudio, сгенерируется речь для для заголовка и описания.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Дополнение успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "204", description = "Нет такой услуги")
    })
    @PostMapping
    public ResponseEntity<AdditionDTO> createAddition(@RequestBody @Valid AdditionDTO additionDTO) {
        try {
            if (additionDTO.getItemId() == 0) {
                return ResponseEntity.badRequest().build();
            }

            Addition newAddition = Addition.builder()
                    .title(additionDTO.getTitle())
                    .description(additionDTO.getDescription())
                    .gifPreview(additionDTO.getGifPreview())
                    .gifLink(additionDTO.getGifLink())
                    .item(itemService.getItemById(additionDTO.getItemId()))
                    .iconLinks(new ArrayList<>())
                    .mainIconLink(additionDTO.getMainIconLink())
                    .audio(new ArrayList<>())
                    .descriptionHash(encodeService.generateHashForAudio(additionDTO.getDescription()))
                    .build();

            additionService.createAddition(newAddition, additionDTO.isEnableAudio());

            return new ResponseEntity<>(additionAssembler.toModel(newAddition), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }


    @Operation(summary = "Получить дополнение по ID", description = "Получает дополнение по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Дополнение найдено"),
            @ApiResponse(responseCode = "204", description = "Дополнение не найдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdditionDTO> getAdditionById(@PathVariable long id) {
        try {
            Addition addition = additionService.getAdditionById(id);
            return ResponseEntity.ok(additionAssembler.toModel(addition));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Удалить дополнение", description = "Удаляет дополнение по его идентификатору.")
    @ApiResponse(responseCode = "204", description = "Дополнение успешно удалено")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddition(@PathVariable long id) {
        additionService.deleteAddition(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить заголовок", description = "Обновляет заголвок дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Заголовок дополнения успешно обновлен")
    @PutMapping("/{id}/title")
    public ResponseEntity<Void> updateAdditionTitle(@PathVariable long id, @RequestBody String title) {
        additionService.updateTitleToAddition(id, title);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить описание дополнения", description = "Обновляет описание дополнения по его идентификатору. Если включить флаг enableAudio, сгенерируется речь описания, иначе - удалится (если есть) или не будет сгененрирована.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Описание дополнения успешно обновлено"),
            @ApiResponse(responseCode = "409", description = "Дубликат файла аудио по названию")
    })
    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescriptionToAddition(
            @PathVariable long id,
            @RequestBody DescriptionUpdateDTO descriptionUpdateDTO) throws IOException {

        String description = descriptionUpdateDTO.getDescription();
        boolean enableAudio = descriptionUpdateDTO.isEnableAudio();

        try {
            additionService.updateDescriptionToAddition(id, description, enableAudio);
            return ResponseEntity.ok().build();
        } catch (FileDuplicateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Обновить ссылку на GIF-превью дополнения", description = "Обновляет ссылку на GIF-превью дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Ссылка на GIF-превью дополнения успешно обновлена")
    @PutMapping("/{id}/gif-preview")
    public ResponseEntity<Void> updateAdditionGifPreview(@PathVariable long id, @RequestBody String gifPreview) {
        additionService.updateGifPreviewToAddition(id, gifPreview);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить ссылку на GIF дополнения", description = "Обновляет ссылку на GIF дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Ссылка на GIF дополнения успешно обновлена")
    @PutMapping("/{id}/gif")
    public ResponseEntity<Void> updateAdditionGifLink(@PathVariable long id, @RequestBody String gifLink) {
        additionService.updateGifLinkToAddition(id, gifLink);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить ссылку на главную иконку дополнения", description = "Обновляет ссылку на главную иконку дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Ссылка на главную иконку дополнения успешно обновлена")
    @PutMapping("/{id}/main-icon")
    public ResponseEntity<Void> updateAdditionMainIcon(@PathVariable long id, @RequestBody String link) {
        additionService.updateMainIconToAddition(id, link);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Найти дополнения по их услуге", description = "Поиск дополнений, которые принадлежат определённой услуге.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Пустой возврат")
    })
    @GetMapping("/search/item/{itemId}")
    public ResponseEntity<Page<AdditionDTO>> findByItemId(@PathVariable long itemId, Pageable pageable) {
        Page<AdditionDTO> additions = additionService.findByItemId(itemId, pageable).map(additionAssembler::toModel);

        if (additions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(additions);
    }

    @Operation(summary = "Добавить иконку дополнения", description = "Добавляет иконку для дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Иконка для дополнения успешно добавлена")
    @PutMapping("/{id}/icon/add")
    public ResponseEntity<Void> addAdditionIcon(@PathVariable long id, @RequestBody String iconLink) {
        additionService.addIcon(id, iconLink);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить иконку дополнения", description = "Удаляет иконку для дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Иконка для дополнения успешно удалена")
    @PutMapping("/{id}/icon/remove")
    public ResponseEntity<Void> removeAdditionIcon(@PathVariable long id, @RequestBody String iconLink) {
        additionService.removeIcon(id, iconLink);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Очистить иконки дополнения", description = "Очищает иконки дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Иконки дополнения очищены")
    @PutMapping("/{id}/clear-icons")
    public ResponseEntity<Void> clearIcons(@PathVariable long id) {
        additionService.clearIcons(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Сгенерировать аудио заголовка дополнения", description = "Генерирует аудио заголовка дополнения по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аудио заголовка дополнения готово"),
            @ApiResponse(responseCode = "409", description = "Дубликат файла аудио по названию")
    })
    @PutMapping("/{id}/title-audio/generate")
    public ResponseEntity<Void> generateTitleAudio(@PathVariable long id) throws IOException {
        try {
            additionService.generateTitleAudio(id);
            return ResponseEntity.ok().build();
        } catch (FileDuplicateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Удалить аудио заголовка дополнения", description = "Удаляет аудио заголовка дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Аудио заголовка дополнения удалено")
    @PutMapping("/{id}/title-audio/remove")
    public ResponseEntity<Void> removeTitleAudio(@PathVariable long id) {
        additionService.removeTitleAudio(id);
        return ResponseEntity.ok().build();
    }
}
