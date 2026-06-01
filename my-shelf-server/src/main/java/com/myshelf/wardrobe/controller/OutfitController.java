package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.service.OutfitService;
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
 * Контроллер для управления образами (комплектами одежды).
 */
@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    private final OutfitService outfitService;

    /**
     * Создаёт контроллер образов.
     *
     * @param outfitService сервис управления образами
     */
    public OutfitController(OutfitService outfitService) {
        this.outfitService = outfitService;
    }

    /**
     * Возвращает список образов текущего пользователя.
     *
     * @param authentication контекст аутентификации
     * @return список образов
     */
    @GetMapping
    public ResponseEntity<List<OutfitDTO>> getUserOutfits(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<OutfitDTO> outfits = outfitService.getOutfitsByUserId(userId);
        return ResponseEntity.ok(outfits);
    }

    /**
     * Возвращает образ по идентификатору.
     *
     * @param id идентификатор образа
     * @return данные образа со слотами
     */
    @GetMapping("/{id}")
    public ResponseEntity<OutfitDTO> getOutfitById(@PathVariable UUID id) {
        OutfitDTO outfit = outfitService.getOutfitById(id);
        return ResponseEntity.ok(outfit);
    }

    /**
     * Создаёт новый образ для текущего пользователя.
     *
     * @param dto данные образа и слотов
     * @param authentication контекст аутентификации
     * @return созданный образ
     */
    @PostMapping
    public ResponseEntity<OutfitDTO> createOutfit(@Valid @RequestBody OutfitDTO dto,
                                                  Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        OutfitDTO createdOutfit = outfitService.createOutfit(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOutfit);
    }

    /**
     * Обновляет существующий образ.
     *
     * @param id идентификатор образа
     * @param dto новые данные образа и слотов
     * @return обновлённый образ
     */
    @PutMapping("/{id}")
    public ResponseEntity<OutfitDTO> updateOutfit(@PathVariable UUID id,
                                                  @Valid @RequestBody OutfitDTO dto) {
        OutfitDTO updatedOutfit = outfitService.updateOutfit(id, dto);
        return ResponseEntity.ok(updatedOutfit);
    }

    /**
     * Удаляет образ по идентификатору.
     *
     * @param id идентификатор образа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutfit(@PathVariable UUID id) {
        outfitService.deleteOutfit(id);
        return ResponseEntity.noContent().build();
    }
}
