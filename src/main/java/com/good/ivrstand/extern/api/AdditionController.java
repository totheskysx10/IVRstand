package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.AdditionService;
import com.good.ivrstand.app.ItemService;
import com.good.ivrstand.domain.Addition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/additions")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "https://good-web-ivr.netlify.app"})
@Tag(name = "AdditionController", description = "Контроллер для управления дополнениями к услугам")
public class AdditionController {

    private final AdditionService additionService;
    private final AdditionAssembler additionAssembler;
    private final ItemService itemService;

    @Autowired
    public AdditionController(AdditionService additionService, AdditionAssembler additionAssembler, ItemService itemService) {
        this.additionService = additionService;
        this.additionAssembler = additionAssembler;
        this.itemService = itemService;
    }

    @Operation(summary = "Создать дополнение", description = "Создает новое дополнение для указанной услуги.")
    @ApiResponse(responseCode = "201", description = "Дополнение успешно создано")
    @PostMapping
    public ResponseEntity<AdditionDTO> createAddition(@RequestBody AdditionDTO additionDTO) {
        if (additionDTO.getItemId() == 0) {
            return ResponseEntity.badRequest().build();
        }

        Addition newAddition = Addition.builder()
                .title(additionDTO.getTitle())
                .description(additionDTO.getDescription())
                .gifLink(additionDTO.getGifLink())
                .item(itemService.getItemById(additionDTO.getItemId()))
                .build();

        additionService.createAddition(newAddition);

        return new ResponseEntity<>(additionAssembler.toModel(newAddition), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить дополнение по ID", description = "Получает дополнение по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Дополнение найдено")
    @GetMapping("/{id}")
    public ResponseEntity<AdditionDTO> getAdditionById(@PathVariable long id) {
        Addition addition = additionService.getAdditionById(id);
        return ResponseEntity.ok(additionAssembler.toModel(addition));
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

    @Operation(summary = "Обновить описание дополнения", description = "Обновляет описание дополнения по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Описание дополнения успешно обновлено")
    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescriptionToAddition(@PathVariable long id, @RequestBody String description) {
        additionService.updateDescriptionToAddition(id, description);
        return ResponseEntity.ok().build();
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
}
