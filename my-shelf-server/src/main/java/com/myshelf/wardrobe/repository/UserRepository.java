package com.myshelf.wardrobe.repository;

import com.myshelf.wardrobe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для управления сущностями User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Найти пользователя по email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверить существование пользователя с данным email.
     */
    boolean existsByEmail(String email);
}
