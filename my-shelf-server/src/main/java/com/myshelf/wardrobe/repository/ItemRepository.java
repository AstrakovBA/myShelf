package com.myshelf.wardrobe.repository;

import com.myshelf.wardrobe.entity.Item;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * Найти все вещи пользователя с жадной загрузкой владельца ({@code user}) в одном запросе.
     * Использует {@link EntityGraph}, чтобы избежать N+1 при обращении к {@code item.getUser()}.
     */
    @EntityGraph(attributePaths = {"user"})
    List<Item> findByUser_Id(UUID userId);

    /**
     * Проверить существование вещи с данным ID у пользователя.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
