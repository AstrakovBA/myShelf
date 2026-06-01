package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.Season;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemService itemService;

    private UUID userId;
    private UUID itemId;
    private User testUser;
    private Item testItem;
    private ItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .displayName("TestUser")
                .build();

        testItem = Item.builder()
                .id(itemId)
                .user(testUser)
                .name("T-Shirt")
                .description("White cotton T-shirt")
                .imageUrl("http://example.com/tshirt.jpg")
                .category(Category.TOP)
                .season(Season.SUMMER)
                .build();

        testItemDTO = new ItemDTO(
                itemId,
                "T-Shirt",
                "White cotton T-shirt",
                "http://example.com/tshirt.jpg",
                Category.TOP,
                Season.SUMMER
        );
    }

    // ===== CREATE =====

    @Test
    @DisplayName("createItem — успешное создание вещи")
    void createItem_success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(itemId);
            return saved;
        });

        // Act
        ItemDTO result = itemService.createItem(userId, testItemDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("T-Shirt");
        assertThat(result.getCategory()).isEqualTo(Category.TOP);
        assertThat(result.getSeason()).isEqualTo(Season.SUMMER);

        verify(userRepository).findById(userId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("createItem — пользователь не найден")
    void createItem_userNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.createItem(userId, testItemDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
    }

    // ===== READ =====

    @Test
    @DisplayName("getItemById — успешное получение вещи")
    void getItemById_success() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));

        // Act
        ItemDTO result = itemService.getItemById(itemId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getName()).isEqualTo("T-Shirt");
        assertThat(result.getCategory()).isEqualTo(Category.TOP);

        verify(itemRepository).findById(itemId);
    }

    @Test
    @DisplayName("getItemById — вещь не найдена")
    void getItemById_notFound() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItemById(itemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь не найдена");

        verify(itemRepository).findById(itemId);
    }

    @Test
    @DisplayName("getItemsByUserId — успешное получение списка вещей")
    void getItemsByUserId_success() {
        // Arrange
        Item item2 = Item.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Jeans")
                .category(Category.BOTTOM)
                .season(Season.ALL_SEASONS)
                .build();

        when(itemRepository.findByUserId(userId)).thenReturn(List.of(testItem, item2));

        // Act
        List<ItemDTO> result = itemService.getItemsByUserId(userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDTO::getName).containsExactly("T-Shirt", "Jeans");
        assertThat(result).extracting(ItemDTO::getCategory)
                .containsExactly(Category.TOP, Category.BOTTOM);

        verify(itemRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getItemsByUserId — пустой список для пользователя")
    void getItemsByUserId_emptyList() {
        // Arrange
        when(itemRepository.findByUserId(userId)).thenReturn(List.of());

        // Act
        List<ItemDTO> result = itemService.getItemsByUserId(userId);

        // Assert
        assertThat(result).isEmpty();
        verify(itemRepository).findByUserId(userId);
    }

    // ===== UPDATE =====

    @Test
    @DisplayName("updateItem — успешное обновление вещи")
    void updateItem_success() {
        // Arrange
        ItemDTO updatedDTO = new ItemDTO(
                itemId,
                "Updated T-Shirt",
                "Updated description",
                "http://example.com/updated.jpg",
                Category.TOP,
                Season.WINTER
        );

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemDTO result = itemService.updateItem(itemId, updatedDTO);

        // Assert
        assertThat(result.getName()).isEqualTo("Updated T-Shirt");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getSeason()).isEqualTo(Season.WINTER);

        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(testItem);
    }

    @Test
    @DisplayName("updateItem — вещь не найдена")
    void updateItem_notFound() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.updateItem(itemId, testItemDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь не найдена");

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    // ===== DELETE =====

    @Test
    @DisplayName("deleteItem — успешное удаление вещи")
    void deleteItem_success() {
        // Arrange
        when(itemRepository.existsById(itemId)).thenReturn(true);

        // Act
        itemService.deleteItem(itemId);

        // Assert
        verify(itemRepository).existsById(itemId);
        verify(itemRepository).deleteById(itemId);
    }

    @Test
    @DisplayName("deleteItem — вещь не найдена при удалении")
    void deleteItem_notFound() {
        // Arrange
        when(itemRepository.existsById(itemId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> itemService.deleteItem(itemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь не найдена");

        verify(itemRepository).existsById(itemId);
        verify(itemRepository, never()).deleteById(any());
    }
}
