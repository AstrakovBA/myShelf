package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления вещами (предметами гардероба).
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Получение всех вещей текущего пользователя.
     * GET /api/items
     */
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getUserItems(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<ItemDTO> items = itemService.getItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Получение вещи по ID.
     * GET /api/items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable UUID id) {
        ItemDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * Создание новой вещи.
     * POST /api/items
     */
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO dto,
                                              Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        ItemDTO createdItem = itemService.createItem(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    /**
     * Обновление вещи.
     * PUT /api/items/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(@PathVariable UUID id,
                                              @Valid @RequestBody ItemDTO dto) {
        ItemDTO updatedItem = itemService.updateItem(id, dto);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Удаление вещи.
     * DELETE /api/items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
