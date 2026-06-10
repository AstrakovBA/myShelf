package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.dto.OutfitSlotDTO;
import com.myshelf.wardrobe.entity.*;
import com.myshelf.wardrobe.mapper.OutfitMapper;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.OutfitRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import jakarta.persistence.EntityNotFoundException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutfitServiceTest {

    @Mock
    private OutfitRepository outfitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private OutfitMapper outfitMapper = new OutfitMapper();

    @InjectMocks
    private OutfitService outfitService;

    private UUID userId;
    private UUID outfitId;
    private UUID itemId;
    private User testUser;
    private Item testItem;
    private Outfit testOutfit;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        outfitId = UUID.randomUUID();
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

        testOutfit = Outfit.builder()
                .id(outfitId)
                .user(testUser)
                .name("Summer Outfit")
                .description("Casual summer look")
                .slots(new ArrayList<>())
                .build();

        // Добавляем слот в образ для тестов чтения
        OutfitSlot slot = OutfitSlot.builder()
                .id(UUID.randomUUID())
                .outfit(testOutfit)
                .item(testItem)
                .slotType(Category.TOP)
                .build();
        testOutfit.addSlot(slot);
    }

    // ===== CREATE =====

    @Test
    @DisplayName("createOutfit — успешное создание образа с валидными слотами")
    void createOutfit_success() {
        // Arrange
        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Summer Outfit", "Casual summer look", List.of(slotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));
        when(outfitRepository.save(any(Outfit.class))).thenAnswer(invocation -> {
            Outfit saved = invocation.getArgument(0);
            saved.setId(outfitId);
            return saved;
        });

        // Act
        OutfitDTO result = outfitService.createOutfit(userId, outfitDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Summer Outfit");
        assertThat(result.getDescription()).isEqualTo("Casual summer look");
        assertThat(result.getSlots()).hasSize(1);
        assertThat(result.getSlots().get(0).getSlotType()).isEqualTo(Category.TOP);
        assertThat(result.getSlots().get(0).getItemId()).isEqualTo(itemId);

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(outfitRepository).save(any(Outfit.class));
    }

    @Test
    @DisplayName("createOutfit — успешное создание образа с пустым слотом (без вещи)")
    void createOutfit_successWithEmptySlot() {
        // Arrange
        OutfitSlotDTO emptySlotDTO = new OutfitSlotDTO(null, null, Category.BOTTOM);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Template Outfit", null, List.of(emptySlotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(outfitRepository.save(any(Outfit.class))).thenAnswer(invocation -> {
            Outfit saved = invocation.getArgument(0);
            saved.setId(outfitId);
            return saved;
        });

        // Act
        OutfitDTO result = outfitService.createOutfit(userId, outfitDTO);

        // Assert
        assertThat(result.getSlots()).hasSize(1);
        assertThat(result.getSlots().get(0).getSlotType()).isEqualTo(Category.BOTTOM);
        assertThat(result.getSlots().get(0).getItemId()).isNull();

        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
        verify(outfitRepository).save(any(Outfit.class));
    }

    @Test
    @DisplayName("createOutfit — пользователь не найден")
    void createOutfit_userNotFound() {
        // Arrange
        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Outfit", null, List.of(slotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> outfitService.createOutfit(userId, outfitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository, outfitRepository);
    }

    @Test
    @DisplayName("createOutfit — вещь не найдена по itemId")
    void createOutfit_itemNotFound() {
        // Arrange
        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Outfit", null, List.of(slotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> outfitService.createOutfit(userId, outfitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь с ID")
                .hasMessageContaining("не найдена");

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(outfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOutfit — несоответствие категории вещи типу слота")
    void createOutfit_categoryMismatch() {
        // Arrange
        // Слот ожидает BOTTOM, но вещь имеет категорию TOP
        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.BOTTOM);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Outfit", null, List.of(slotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));

        // Act & Assert
        assertThatThrownBy(() -> outfitService.createOutfit(userId, outfitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Категория вещи 'TOP' не соответствует типу слота 'BOTTOM'");

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(outfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOutfit — вещь не принадлежит текущему пользователю")
    void createOutfit_itemNotOwnedByUser() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder()
                .id(otherUserId)
                .email("other@example.com")
                .passwordHash("otherHash")
                .displayName("OtherUser")
                .build();

        Item otherUserItem = Item.builder()
                .id(itemId)
                .user(otherUser)
                .name("Other's Jacket")
                .category(Category.OUTERWEAR)
                .season(Season.WINTER)
                .build();

        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.OUTERWEAR);
        OutfitDTO outfitDTO = new OutfitDTO(null, "Outfit", null, List.of(slotDTO));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(otherUserItem));

        // Act & Assert
        assertThatThrownBy(() -> outfitService.createOutfit(userId, outfitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь не принадлежит текущему пользователю");

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(outfitRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOutfit — создание образа без слотов (null slots)")
    void createOutfit_nullSlots() {
        // Arrange
        OutfitDTO outfitDTO = new OutfitDTO(null, "Empty Outfit", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(outfitRepository.save(any(Outfit.class))).thenAnswer(invocation -> {
            Outfit saved = invocation.getArgument(0);
            saved.setId(outfitId);
            return saved;
        });

        // Act
        OutfitDTO result = outfitService.createOutfit(userId, outfitDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Empty Outfit");
        assertThat(result.getSlots()).isEmpty();

        verify(userRepository).findById(userId);
        verify(outfitRepository).save(any(Outfit.class));
    }

    // ===== READ =====

    @Test
    @DisplayName("getOutfitById — успешное получение образа")
    void getOutfitById_success() {
        // Arrange
        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.of(testOutfit));

        // Act
        OutfitDTO result = outfitService.getOutfitById(outfitId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(outfitId);
        assertThat(result.getName()).isEqualTo("Summer Outfit");
        assertThat(result.getSlots()).hasSize(1);
        assertThat(result.getSlots().get(0).getSlotType()).isEqualTo(Category.TOP);

        verify(outfitRepository).findWithSlots(outfitId);
    }

    @Test
    @DisplayName("getOutfitById — образ не найден")
    void getOutfitById_notFound() {
        // Arrange
        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> outfitService.getOutfitById(outfitId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Образ не найден");

        verify(outfitRepository).findWithSlots(outfitId);
    }

    @Test
    @DisplayName("getOutfitsByUserId — успешное получение списка образов")
    void getOutfitsByUserId_success() {
        // Arrange
        Outfit outfit2 = Outfit.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Winter Outfit")
                .description("")
                .slots(new ArrayList<>())
                .build();

        when(outfitRepository.findByUserId(userId)).thenReturn(List.of(testOutfit, outfit2));

        // Act
        List<OutfitDTO> result = outfitService.getOutfitsByUserId(userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OutfitDTO::getName)
                .containsExactly("Summer Outfit", "Winter Outfit");

        verify(outfitRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getOutfitsByUserId — пустой список")
    void getOutfitsByUserId_emptyList() {
        // Arrange
        when(outfitRepository.findByUserId(userId)).thenReturn(List.of());

        // Act
        List<OutfitDTO> result = outfitService.getOutfitsByUserId(userId);

        // Assert
        assertThat(result).isEmpty();
        verify(outfitRepository).findByUserId(userId);
    }

    // ===== UPDATE =====

    @Test
    @DisplayName("updateOutfit — успешное обновление образа")
    void updateOutfit_success() {
        // Arrange
        UUID newItemId = UUID.randomUUID();
        Item newItem = Item.builder()
                .id(newItemId)
                .user(testUser)
                .name("Jeans")
                .category(Category.BOTTOM)
                .season(Season.ALL_SEASONS)
                .build();

        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, newItemId, Category.BOTTOM);
        OutfitDTO updateDTO = new OutfitDTO(outfitId, "Updated Outfit", "New description", List.of(slotDTO));

        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.of(testOutfit));
        when(itemRepository.findById(newItemId)).thenReturn(Optional.of(newItem));
        when(outfitRepository.save(any(Outfit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OutfitDTO result = outfitService.updateOutfit(outfitId, userId, updateDTO);

        // Assert
        assertThat(result.getName()).isEqualTo("Updated Outfit");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getSlots()).hasSize(1);
        assertThat(result.getSlots().get(0).getSlotType()).isEqualTo(Category.BOTTOM);

        verify(outfitRepository).findWithSlots(outfitId);
        verify(itemRepository).findById(newItemId);
        verify(outfitRepository).save(testOutfit);
    }

    @Test
    @DisplayName("updateOutfit — образ не найден")
    void updateOutfit_notFound() {
        // Arrange
        OutfitDTO updateDTO = new OutfitDTO(outfitId, "Updated", null, List.of());
        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> outfitService.updateOutfit(outfitId, userId, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Образ не найден");

        verify(outfitRepository).findWithSlots(outfitId);
        verify(itemRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateOutfit — несоответствие категории при обновлении")
    void updateOutfit_categoryMismatch() {
        // Arrange
        // Слот типа TOP, но вещь категории BOTTOM
        OutfitSlotDTO slotDTO = new OutfitSlotDTO(null, itemId, Category.TOP);
        OutfitDTO updateDTO = new OutfitDTO(outfitId, "Updated", null, List.of(slotDTO));

        // testItem имеет категорию TOP, но мы укажем слот типа BOTTOM для конфликта
        OutfitSlotDTO mismatchSlotDTO = new OutfitSlotDTO(null, itemId, Category.SHOES);
        OutfitDTO mismatchDTO = new OutfitDTO(outfitId, "Updated", null, List.of(mismatchSlotDTO));

        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.of(testOutfit));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(testItem));

        // Act & Assert
        assertThatThrownBy(() -> outfitService.updateOutfit(outfitId, userId, mismatchDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Категория вещи 'TOP' не соответствует типу слота 'SHOES'");
    }

    @Test
    @DisplayName("updateOutfit — образ принадлежит другому пользователю")
    void updateOutfit_accessDenied() {
        UUID otherUserId = UUID.randomUUID();
        OutfitDTO updateDTO = new OutfitDTO(outfitId, "Updated", null, List.of());
        when(outfitRepository.findWithSlots(outfitId)).thenReturn(Optional.of(testOutfit));

        assertThatThrownBy(() -> outfitService.updateOutfit(outfitId, otherUserId, updateDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа");

        verify(outfitRepository, never()).save(any());
    }

    // ===== DELETE =====

    @Test
    @DisplayName("deleteOutfit — успешное удаление образа")
    void deleteOutfit_success() {
        when(outfitRepository.findById(outfitId)).thenReturn(Optional.of(testOutfit));

        outfitService.deleteOutfit(outfitId, userId);

        verify(outfitRepository).findById(outfitId);
        verify(outfitRepository).delete(testOutfit);
    }

    @Test
    @DisplayName("deleteOutfit — образ не найден при удалении")
    void deleteOutfit_notFound() {
        when(outfitRepository.findById(outfitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> outfitService.deleteOutfit(outfitId, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Образ не найден");

        verify(outfitRepository).findById(outfitId);
        verify(outfitRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteOutfit — образ принадлежит другому пользователю")
    void deleteOutfit_accessDenied() {
        UUID otherUserId = UUID.randomUUID();
        when(outfitRepository.findById(outfitId)).thenReturn(Optional.of(testOutfit));

        assertThatThrownBy(() -> outfitService.deleteOutfit(outfitId, otherUserId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа");

        verify(outfitRepository, never()).delete(any());
    }
}
