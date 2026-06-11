package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.dto.OutfitSlotDTO;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.Outfit;
import com.myshelf.wardrobe.entity.OutfitSlot;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.mapper.OutfitMapper;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.OutfitRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OutfitMapper outfitMapper;

    /**
     * Создаёт сервис образов.
     *
     * @param outfitRepository репозиторий образов
     * @param userRepository репозиторий пользователей
     * @param itemRepository репозиторий вещей
     * @param outfitMapper маппер Outfit ↔ DTO
     */
    public OutfitService(OutfitRepository outfitRepository,
                         UserRepository userRepository,
                         ItemRepository itemRepository,
                         OutfitMapper outfitMapper) {
        this.outfitRepository = outfitRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.outfitMapper = outfitMapper;
    }

    /**
     * Создаёт новый образ с валидацией слотов и вещей.
     *
     * @param userId идентификатор владельца
     * @param outfitDTO данные образа и слотов
     * @return сохранённый образ
     */
    public OutfitDTO createOutfit(UUID userId, OutfitDTO outfitDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Outfit outfit = outfitMapper.toEntity(outfitDTO, user);
        applySlotsFromDTO(outfit, outfitDTO.getSlots(), userId);

        Outfit savedOutfit = outfitRepository.save(outfit);
        return outfitMapper.toDTO(savedOutfit);
    }

    /**
     * Возвращает образ по идентификатору (делегирует {@link #getOutfitDetails(UUID)}).
     *
     * @param outfitId идентификатор образа
     * @return DTO образа со слотами
     */
    @Transactional(readOnly = true)
    public OutfitDTO getOutfitById(UUID outfitId) {
        return getOutfitDetails(outfitId);
    }

    /**
     * Возвращает образ со слотами, загруженными через JOIN FETCH.
     *
     * @param outfitId идентификатор образа
     * @return DTO образа со слотами
     */
    @Transactional(readOnly = true)
    public OutfitDTO getOutfitDetails(UUID outfitId) {
        Outfit outfit = outfitRepository.findWithSlots(outfitId)
                .orElseThrow(() -> new IllegalArgumentException("Образ не найден"));
        return outfitMapper.toDTO(outfit);
    }

    /**
     * Возвращает все образы пользователя.
     *
     * @param userId идентификатор владельца
     * @return список DTO образов
     */
    @Transactional(readOnly = true)
    public List<OutfitDTO> getOutfitsByUserId(UUID userId) {
        List<Outfit> outfits = outfitRepository.findByUserId(userId);
        return outfits.stream()
                .map(outfitMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет образ и его слоты.
     *
     * @param outfitId идентификатор образа
     * @param outfitDTO новые данные образа и слотов
     * @return обновлённый образ
     */
    public OutfitDTO updateOutfit(UUID outfitId, UUID currentUserId, OutfitDTO outfitDTO) {
        Outfit outfit = outfitRepository.findWithSlots(outfitId)
                .orElseThrow(() -> new EntityNotFoundException("Образ не найден"));

        if (!outfit.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Нет доступа к этому образу");
        }

        outfitMapper.updateEntityFromDTO(outfit, outfitDTO);
        outfit.getSlots().clear();
        applySlotsFromDTO(outfit, outfitDTO.getSlots(), currentUserId);

        Outfit updatedOutfit = outfitRepository.save(outfit);
        return outfitMapper.toDTO(updatedOutfit);
    }

    /**
     * Удаляет образ по идентификатору.
     *
     * @param outfitId идентификатор образа
     */
    public void deleteOutfit(UUID outfitId, UUID currentUserId) {
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new EntityNotFoundException("Образ не найден"));

        if (!outfit.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Нет доступа к этому образу");
        }

        outfitRepository.delete(outfit);
    }

    private void applySlotsFromDTO(Outfit outfit, List<OutfitSlotDTO> slotDTOs, UUID userId) {
        if (slotDTOs == null) {
            return;
        }
        for (OutfitSlotDTO slotDTO : slotDTOs) {
            Item item = resolveSlotItem(slotDTO, userId);
            OutfitSlot slot = outfitMapper.toSlotEntity(slotDTO, outfit, item);
            outfit.addSlot(slot);
        }
    }

    private Item resolveSlotItem(OutfitSlotDTO slotDTO, UUID userId) {
        if (slotDTO.getItemId() == null) {
            return null;
        }

        Item item = itemRepository.findById(slotDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Вещь с ID " + slotDTO.getItemId() + " не найдена"));

        if (item.getCategory() != slotDTO.getSlotType()) {
            throw new IllegalArgumentException(
                    "Категория вещи '" + item.getCategory() +
                    "' не соответствует типу слота '" + slotDTO.getSlotType() + "'");
        }

        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Вещь не принадлежит текущему пользователю");
        }

        return item;
    }
}
