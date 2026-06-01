package com.myshelf.wardrobe.repository;

import com.myshelf.wardrobe.entity.Outfit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для управления сущностями Outfit.
 */
@Repository
public interface OutfitRepository extends JpaRepository<Outfit, UUID> {

    /**
     * Найти все образы конкретного пользователя.
     */
    List<Outfit> findByUserId(UUID userId);

    /**
     * Загрузить образ вместе со слотами одним запросом ({@code JOIN FETCH}).
     */
    @Query("SELECT o FROM Outfit o LEFT JOIN FETCH o.slots WHERE o.id = :id")
    Optional<Outfit> findWithSlots(@Param("id") UUID id);

    /**
     * Проверить существование образа с данным ID у пользователя.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
