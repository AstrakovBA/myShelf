package com.myshelf.wardrobe.jpa;

import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.Season;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Интеграционная демонстрация Identity Map через Persistence Context Hibernate.
 * <p>
 * В рамках одной транзакции {@link EntityManager} хранит не более одного managed-экземпляра
 * на каждый первичный ключ.
 */
@DataJpaTest
@ActiveProfiles("test")
class IdentityMapTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private UUID itemId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("identity-map@test.example")
                .passwordHash("hash")
                .displayName("IdentityMapUser")
                .build();
        entityManager.persist(user);

        Item item = Item.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name("Original name")
                .category(Category.TOP)
                .season(Season.SUMMER)
                .build();
        entityManager.persist(item);
        entityManager.flush();

        itemId = item.getId();
    }

    @Test
    @DisplayName("EntityManager: две загрузки по id — один объект (Identity Map)")
    void entityManager_returnsSameManagedInstanceForSameId() {
        Item first = entityManager.find(Item.class, itemId);
        Item second = entityManager.find(Item.class, itemId);

        assertSame(first, second, "Identity Map: обе ссылки должны указывать на один managed-объект");

        first.setName("Changed via first reference");
        entityManager.flush();

        assertThat(second.getName())
                .as("изменение через first видно в second — общий экземпляр в Persistence Context")
                .isEqualTo("Changed via first reference");
    }

    @Test
    @DisplayName("Spring Data JPA: два findById в одной транзакции — один объект")
    void repository_returnsSameManagedInstanceForSameId() {
        Item first = itemRepository.findById(itemId).orElseThrow();
        Item second = itemRepository.findById(itemId).orElseThrow();

        assertSame(first, second);

        first.setDescription("Updated description");

        assertThat(second.getDescription())
                .isEqualTo("Updated description");
    }
}
