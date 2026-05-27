package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.service.OutfitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления образами (комплектами одежды).
 */
@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    private final OutfitService outfitService;

    public OutfitController(OutfitService outfitService) {
        this.outfitService = outfitService;
    }

    /**
     * Получение всех образов текущего пользователя.
     * GET /api/outfits
     */
    @GetMapping
    public ResponseEntity<List<OutfitDTO>> getUserOutfits(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<OutfitDTO> outfits = outfitService.getOutfitsByUserId(userId);
        return ResponseEntity.ok(outfits);
    }

    /**
     * Получение образа по ID.
     * GET /api/outfits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OutfitDTO> getOutfitById(@PathVariable UUID id) {
        OutfitDTO outfit = outfitService.getOutfitById(id);
        return ResponseEntity.ok(outfit);
    }

    /**
     * Создание нового образа.
     * POST /api/outfits
     */
    @PostMapping
    public ResponseEntity<OutfitDTO> createOutfit(@Valid @RequestBody OutfitDTO dto,
                                                  Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        OutfitDTO createdOutfit = outfitService.createOutfit(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOutfit);
    }

    /**
     * Обновление образа.
     * PUT /api/outfits/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<OutfitDTO> updateOutfit(@PathVariable UUID id,
                                                  @Valid @RequestBody OutfitDTO dto) {
        OutfitDTO updatedOutfit = outfitService.updateOutfit(id, dto);
        return ResponseEntity.ok(updatedOutfit);
    }

    /**
     * Удаление образа.
     * DELETE /api/outfits/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutfit(@PathVariable UUID id) {
        outfitService.deleteOutfit(id);
        return ResponseEntity.noContent().build();
    }
}
