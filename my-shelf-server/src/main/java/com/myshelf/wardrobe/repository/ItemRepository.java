package com.myshelf.wardrobe.repository;

import com.myshelf.wardrobe.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для управления сущностями Item.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    /**
     * Найти все вещи конкретного пользователя.
     */
    List<Item> findByUserId(UUID userId);

    /**
     * Проверить существование вещи с данным ID у пользователя.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
