package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления вещами (предметами гардероба).
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * Создаёт контроллер вещей гардероба.
     *
     * @param itemService сервис управления вещами
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Возвращает список вещей текущего пользователя.
     *
     * @param authentication контекст аутентификации
     * @return список вещей
     */
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getUserItems(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<ItemDTO> items = itemService.getItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Возвращает вещь по идентификатору.
     *
     * @param id идентификатор вещи
     * @return данные вещи
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable UUID id) {
        ItemDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * Создаёт новую вещь для текущего пользователя.
     *
     * @param dto данные вещи
     * @param authentication контекст аутентификации
     * @return созданная вещь
     */
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO dto,
                                              Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        ItemDTO createdItem = itemService.createItem(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    /**
     * Обновляет существующую вещь.
     *
     * @param id идентификатор вещи
     * @param dto новые данные вещи
     * @return обновлённая вещь
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(@PathVariable UUID id,
                                              @Valid @RequestBody ItemDTO dto) {
        ItemDTO updatedItem = itemService.updateItem(id, dto);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Удаляет вещь по идентификатору.
     *
     * @param id идентификатор вещи
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
