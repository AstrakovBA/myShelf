package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.dto.OutfitSlotDTO;
import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.Outfit;
import com.myshelf.wardrobe.entity.OutfitSlot;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.OutfitRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для управления образами (комплектами одежды).
 */
@Service
@Transactional
public class OutfitService {

    private final OutfitRepository outfitRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public OutfitService(OutfitRepository outfitRepository,
                         UserRepository userRepository,
                         ItemRepository itemRepository) {
        this.outfitRepository = outfitRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Создание нового образа.
     * Валидация: категория вещи в слоте должна совпадать с типом слота.
     */
    public OutfitDTO createOutfit(UUID userId, OutfitDTO outfitDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Outfit outfit = Outfit.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name(outfitDTO.getName())
                .description(outfitDTO.getDescription())
                .slots(new ArrayList<>())
                .build();

        // Создаем слоты и выполняем валидацию
        if (outfitDTO.getSlots() != null) {
            for (OutfitSlotDTO slotDTO : outfitDTO.getSlots()) {
                // Если itemId указан, проверяем соответствие категории типу слота
                if (slotDTO.getItemId() != null) {
                    Item item = itemRepository.findById(slotDTO.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Вещь с ID " + slotDTO.getItemId() + " не найдена"));

                    // Валидация: категория вещи должна совпадать с типом слота
                    if (item.getCategory() != slotDTO.getSlotType()) {
                        throw new IllegalArgumentException(
                                "Категория вещи '" + item.getCategory() + 
                                "' не соответствует типу слота '" + slotDTO.getSlotType() + "'");
                    }

                    // Проверяем, что вещь принадлежит текущему пользователю
                    if (!item.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException(
                                "Вещь не принадлежит текущему пользователю");
                    }

                    OutfitSlot slot = OutfitSlot.builder()
                            .id(UUID.randomUUID())
                            .outfit(outfit)
                            .item(item)
                            .slotType(slotDTO.getSlotType())
                            .build();

                    outfit.addSlot(slot);
                } else {
                    // Слот без вещи (пустой слот)
                    OutfitSlot slot = OutfitSlot.builder()
                            .id(UUID.randomUUID())
                            .outfit(outfit)
                            .item(null)
                            .slotType(slotDTO.getSlotType())
                            .build();

                    outfit.addSlot(slot);
                }
            }
        }

        Outfit savedOutfit = outfitRepository.save(outfit);
        return convertToDTO(savedOutfit);
    }

    /**
     * Получение образа по ID.
     */
    @Transactional(readOnly = true)
    public OutfitDTO getOutfitById(UUID outfitId) {
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new IllegalArgumentException("Образ не найден"));
        return convertToDTO(outfit);
    }

    /**
     * Получение всех образов пользователя.
     */
    @Transactional(readOnly = true)
    public List<OutfitDTO> getOutfitsByUserId(UUID userId) {
        List<Outfit> outfits = outfitRepository.findByUserId(userId);
        return outfits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление образа.
     */
    public OutfitDTO updateOutfit(UUID outfitId, OutfitDTO outfitDTO) {
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new IllegalArgumentException("Образ не найден"));

        outfit.setName(outfitDTO.getName());
        outfit.setDescription(outfitDTO.getDescription());

        // Очищаем старые слоты
        outfit.getSlots().clear();

        // Добавляем новые слоты с валидацией
        if (outfitDTO.getSlots() != null) {
            for (OutfitSlotDTO slotDTO : outfitDTO.getSlots()) {
                if (slotDTO.getItemId() != null) {
                    Item item = itemRepository.findById(slotDTO.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Вещь с ID " + slotDTO.getItemId() + " не найдена"));

                    // Валидация: категория вещи должна совпадать с типом слота
                    if (item.getCategory() != slotDTO.getSlotType()) {
                        throw new IllegalArgumentException(
                                "Категория вещи '" + item.getCategory() + 
                                "' не соответствует типу слота '" + slotDTO.getSlotType() + "'");
                    }

                    OutfitSlot slot = OutfitSlot.builder()
                            .id(UUID.randomUUID())
                            .outfit(outfit)
                            .item(item)
                            .slotType(slotDTO.getSlotType())
                            .build();

                    outfit.addSlot(slot);
                } else {
                    OutfitSlot slot = OutfitSlot.builder()
                            .id(UUID.randomUUID())
                            .outfit(outfit)
                            .item(null)
                            .slotType(slotDTO.getSlotType())
                            .build();

                    outfit.addSlot(slot);
                }
            }
        }

        Outfit updatedOutfit = outfitRepository.save(outfit);
        return convertToDTO(updatedOutfit);
    }

    /**
     * Удаление образа.
     */
    public void deleteOutfit(UUID outfitId) {
        if (!outfitRepository.existsById(outfitId)) {
            throw new IllegalArgumentException("Образ не найден");
        }
        outfitRepository.deleteById(outfitId);
    }

    private OutfitDTO convertToDTO(Outfit outfit) {
        List<OutfitSlotDTO> slotDTOs = outfit.getSlots().stream()
                .map(this::convertSlotToDTO)
                .collect(Collectors.toList());

        return new OutfitDTO(
                outfit.getId(),
                outfit.getName(),
                outfit.getDescription(),
                slotDTOs
        );
    }

    private OutfitSlotDTO convertSlotToDTO(OutfitSlot slot) {
        UUID itemId = slot.getItem() != null ? slot.getItem().getId() : null;
        return new OutfitSlotDTO(
                slot.getId(),
                itemId,
                slot.getSlotType()
        );
    }
}
