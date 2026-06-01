package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для управления вещами (предметами гардероба).
 */
@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание новой вещи.
     */
    public ItemDTO createItem(UUID userId, ItemDTO itemDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Item item = Item.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .imageUrl(itemDTO.getImageUrl())
                .category(itemDTO.getCategory())
                .season(itemDTO.getSeason())
                .build();

        Item savedItem = itemRepository.save(item);
        return convertToDTO(savedItem);
    }

    /**
     * Получение вещи по ID.
     */
    @Transactional(readOnly = true)
    public ItemDTO getItemById(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена"));
        return convertToDTO(item);
    }

    /**
     * Получение всех вещей пользователя.
     */
    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByUserId(UUID userId) {
        List<Item> items = itemRepository.findByUserId(userId);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление вещи.
     */
    public ItemDTO updateItem(UUID itemId, ItemDTO itemDTO) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена"));

        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setImageUrl(itemDTO.getImageUrl());
        item.setCategory(itemDTO.getCategory());
        item.setSeason(itemDTO.getSeason());

        Item updatedItem = itemRepository.save(item);
        return convertToDTO(updatedItem);
    }

    /**
     * Удаление вещи.
     */
    public void deleteItem(UUID itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Вещь не найдена");
        }
        itemRepository.deleteById(itemId);
    }

    private ItemDTO convertToDTO(Item item) {
        return new ItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getImageUrl(),
                item.getCategory(),
                item.getSeason()
        );
    }
}
