# Спецификация интерфейсов и контрактов Java

## 1. Описание контрактов между слоями

В адаптированной архитектуре **PCMEF** взаимодействие между слоями серверной части строго регламентировано принципом строгой иерархии.

🔹 **Слой Control (Контроллеры)** выступает единственным шлюзом для внешних запросов. Он принимает данные от клиента в формате JSON, выполняет первичную валидацию входных параметров и преобразует их в DTO (*Data Transfer Objects*). Контроллер не содержит бизнес-логики и не работает напрямую с базой данных. Он делегирует обработку запросов слою `Mediator`.

🔹 **Слой Mediator (Сервисы)** отвечает за реализацию бизнес-правил и координацию транзакций. Сервисы принимают DTO от контроллеров, преобразуют их во внутренние сущности `Entity`, выполняют необходимые проверки (например, уникальность email, корректность категорий вещей в образе) и вызывают слой `Foundation` для сохранения или чтения данных. `Mediator` гарантирует, что состояние системы остается согласованным, и возвращает результат обработки обратно в `Control`, который затем формирует HTTP-ответ.

🔹 **Слой Foundation (Репозитории)** инкапсулирует доступ к базе данных PostgreSQL. Интерфейсы репозиториев расширяют стандартный `JpaRepository` и предоставляют методы для выполнения CRUD-операций над сущностями JPA. Репозитории не знают о бизнес-логике и работают исключительно с объектами `Entity`.

🔹 **Слой Entity** описывает структуру данных и маппинг на таблицы базы данных, являясь пассивным носителем состояния, который передается между `Foundation` и `Mediator`.

---

## 2. Реализация Java-интерфейсов

Ниже приведены определения интерфейсов и классов для слоёв `Mediator` и `Foundation`, обеспечивающие контракт взаимодействия между бизнес-логикой и доступом к данным.

### 2.1. Слой Control (REST API Контракт)
Контроллеры реализуют REST API, используя аннотации Spring MVC. Ниже приведен пример контракта для управления вещами.

#### 2.1.1. `AuthController`
```java
package com.myshelf.control;

import com.myshelf.dto.request.LoginRequest;
import com.myshelf.dto.request.RegisterRequest;
import com.myshelf.dto.response.AuthResponse;
import com.myshelf.dto.response.UserProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public interface AuthController {
    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @GetMapping("/me")
    ResponseEntity<UserProfileDto> getCurrentUserProfile(@RequestHeader("Authorization") String token);
}
```

#### 2.1.2. `ItemController`
```java
package com.myshelf.control;

import com.myshelf.dto.request.ItemRequest;
import com.myshelf.dto.response.ItemDto;
import com.myshelf.enums.Category;
import com.myshelf.enums.Season;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public interface ItemController {
    @GetMapping
    ResponseEntity<List<ItemDto>> getAllItems(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Season season,
            @RequestParam(required = false) String search);

    @GetMapping("/{id}")
    ResponseEntity<ItemDto> getItem(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id);

    @PostMapping
    ResponseEntity<ItemDto> createItem(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody ItemRequest request);

    @PutMapping("/{id}")
    ResponseEntity<ItemDto> updateItem(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id, @Valid @RequestBody ItemRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteItem(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id);
}
```

#### 2.1.3. `OutfitController`
```java
package com.myshelf.control;

import com.myshelf.dto.request.OutfitRequest;
import com.myshelf.dto.response.OutfitDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/outfits")
public interface OutfitController {
    @GetMapping
    ResponseEntity<List<OutfitDto>> getAllOutfits(@RequestHeader("X-User-Id") UUID userId);

    @GetMapping("/{id}")
    ResponseEntity<OutfitDto> getOutfit(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id);

    @PostMapping
    ResponseEntity<OutfitDto> createOutfit(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody OutfitRequest request);

    @PutMapping("/{id}")
    ResponseEntity<OutfitDto> updateOutfit(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id, @Valid @RequestBody OutfitRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteOutfit(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id);
}
```

#### 2.1.4. `UserController`
```java
package com.myshelf.control;

import com.myshelf.dto.request.ChangePasswordRequest;
import com.myshelf.dto.request.UpdateProfileRequest;
import com.myshelf.dto.response.UserProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public interface UserController {
    @GetMapping("/{id}")
    ResponseEntity<UserProfileDto> getUserProfile(@PathVariable UUID id);

    @PutMapping("/{id}")
    ResponseEntity<UserProfileDto> updateProfile(@PathVariable UUID id, @Valid @RequestBody UpdateProfileRequest request);

    @PostMapping("/{id}/change-password")
    ResponseEntity<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteAccount(@PathVariable UUID id);
}
```

### 2.2. Слой Mediator (Сервисы)
Слой `Mediator` инкапсулирует бизнес-логику приложения и координирует взаимодействие между контроллерами и репозиториями. Сервисы выполняют валидацию бизнес-правил, управляют транзакциями и обеспечивают целостность данных.

#### 2.2.1. `AuthService`
```java
package com.myshelf.mediator;

import com.myshelf.dto.request.LoginRequest;
import com.myshelf.dto.request.RegisterRequest;
import com.myshelf.dto.response.AuthResponse;
import com.myshelf.dto.response.UserProfileDto;
import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileDto getCurrentUserProfile(UUID userId);
    boolean validateToken(String token);
    UUID extractUserIdFromToken(String token);
}
```

#### 2.2.2. `ItemService`
```java
package com.myshelf.mediator;

import com.myshelf.dto.request.ItemRequest;
import com.myshelf.dto.response.ItemDto;
import com.myshelf.enums.Category;
import com.myshelf.enums.Season;
import java.util.List;
import java.util.UUID;

public interface ItemService {
    ItemDto createItem(UUID ownerId, ItemRequest request);
    ItemDto getItem(UUID ownerId, UUID itemId);
    List<ItemDto> getAllItems(UUID ownerId);
    List<ItemDto> getItemsByCategory(UUID ownerId, Category category);
    List<ItemDto> getItemsBySeason(UUID ownerId, Season season);
    List<ItemDto> searchItems(UUID ownerId, String query);
    ItemDto updateItem(UUID ownerId, UUID itemId, ItemRequest request);
    void deleteItem(UUID ownerId, UUID itemId);
}
```

#### 2.2.3. `OutfitService`
```java
package com.myshelf.mediator;

import com.myshelf.dto.request.OutfitRequest;
import com.myshelf.dto.response.OutfitDto;
import java.util.List;
import java.util.UUID;

public interface OutfitService {
    OutfitDto createOutfit(UUID ownerId, OutfitRequest request);
    OutfitDto getOutfit(UUID ownerId, UUID outfitId);
    List<OutfitDto> getAllOutfits(UUID ownerId);
    OutfitDto updateOutfit(UUID ownerId, UUID outfitId, OutfitRequest request);
    void deleteOutfit(UUID ownerId, UUID outfitId);
}
```

#### 2.2.4. `UserService`
```java
package com.myshelf.mediator;

import com.myshelf.dto.response.UserProfileDto;
import java.util.UUID;

public interface UserService {
    UserProfileDto getUserProfile(UUID userId);
    UserProfileDto updateProfile(UUID userId, String displayName, String avatarUrl);
    void changePassword(UUID userId, String oldPassword, String newPassword);
    void deleteAccount(UUID userId);
}
```

### 2.3. Слой Entity (Сущности)
Слой `Entity` определяет доменные объекты системы, которые представляют бизнес-сущности предметной области. Каждая сущность аннотируется как JPA-Entity и маппится на соответствующую таблицу базы данных PostgreSQL.

#### 2.3.1. Сущность `User`
```java
package com.myshelf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

#### 2.3.2. Сущность `Item`
```java
package com.myshelf.entity;

import com.myshelf.enums.Category;
import com.myshelf.enums.Season;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season;

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
}
```

#### 2.3.3. Сущность `Outfit`
```java
package com.myshelf.entity;

import com.myshelf.enums.Season;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "outfits")
public class Outfit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season;

    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutfitSlot> slots = new ArrayList<>();

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
    public List<OutfitSlot> getSlots() { return slots; }
    public void setSlots(List<OutfitSlot> slots) { this.slots = slots; }
}
```

#### 2.3.4. Сущность `OutfitSlot`
```java
package com.myshelf.entity;

import com.myshelf.enums.Category;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "outfit_slots")
public class OutfitSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    private Category slotType;

    @Column(nullable = false)
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Outfit getOutfit() { return outfit; }
    public void setOutfit(Outfit outfit) { this.outfit = outfit; }
    public Category getSlotType() { return slotType; }
    public void setSlotType(Category slotType) { this.slotType = slotType; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
}
```

### 2.4. Слой Foundation (Репозитории)
Слой `Foundation` предоставляет абстракцию для доступа к данным через интерфейсы Spring Data JPA Repository. Репозитории инкапсулируют операции CRUD и специфичные запросы к базе данных, не содержа бизнес-правил.

#### 2.4.1. `UserRepository`
```java
package com.myshelf.foundation;

import com.myshelf.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### 2.4.2. `ItemRepository`
```java
package com.myshelf.foundation;

import com.myshelf.entity.Item;
import com.myshelf.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByOwnerId(UUID ownerId);
    List<Item> findByOwnerIdAndCategory(UUID ownerId, Category category);
    void deleteByIdAndOwnerId(UUID id, UUID ownerId);
}
```

#### 2.4.3. `OutfitRepository`
```java
package com.myshelf.foundation;

import com.myshelf.entity.Outfit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutfitRepository extends JpaRepository<Outfit, UUID> {
    List<Outfit> findByOwnerId(UUID ownerId);
    void deleteByIdAndOwnerId(UUID id, UUID ownerId);
}
```

### 2.5. DTO (Data Transfer Objects)
DTO представляют собой простые объекты для передачи данных между слоями и клиентами API. Они инкапсулируют структуру запросов и ответов, обеспечивая разделение между внутренней моделью данных и внешним контрактом API.

#### 2.5.1. Request DTOs
**2.5.1.1. `ItemRequest`**
```java
package com.myshelf.dto.request;

import com.myshelf.enums.Category;
import com.myshelf.enums.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotBlank(message = "Photo URL is required") String photoUrl,
        @NotNull(message = "Category is required") Category category,
        @NotNull(message = "Season is required") Season season
) {}
```

**2.5.1.2. `OutfitSlotRequest`**
```java
package com.myshelf.dto.request;

import com.myshelf.enums.Category;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OutfitSlotRequest(
        @NotNull(message = "Slot type is required") Category slotType,
        @NotNull(message = "Position is required") Integer position,
        UUID itemId
) {}
```

**2.5.1.3. `OutfitRequest`**
```java
package com.myshelf.dto.request;

import com.myshelf.enums.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OutfitRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Season is required") Season season,
        @NotNull(message = "At least one slot is required") List<OutfitSlotRequest> slots
) {}
```

**2.5.1.4. `RegisterRequest`**
```java
package com.myshelf.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Display name is required") String displayName
) {}
```

**2.5.1.5. `LoginRequest`**
```java
package com.myshelf.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,
        @NotBlank(message = "Password is required") String password
) {}
```

#### 2.5.2. Response DTOs
**2.5.2.1. `ItemDto`**
```java
package com.myshelf.dto.response;

import com.myshelf.enums.Category;
import com.myshelf.enums.Season;
import java.util.UUID;

public record ItemDto(
        UUID id,
        String name,
        String description,
        String photoUrl,
        Category category,
        Season season
) {}
```

**2.5.2.2. `OutfitSlotDto`**
```java
package com.myshelf.dto.response;

import com.myshelf.enums.Category;
import java.util.UUID;

public record OutfitSlotDto(
        UUID id,
        Category slotType,
        Integer position,
        UUID itemId,
        String itemPhotoUrl
) {}
```

**2.5.2.3. `OutfitDto`**
```java
package com.myshelf.dto.response;

import com.myshelf.enums.Season;
import java.util.List;
import java.util.UUID;

public record OutfitDto(
        UUID id,
        String name,
        String description,
        Season season,
        List<OutfitSlotDto> slots
) {}
```

**2.5.2.4. `AuthResponse`**
```java
package com.myshelf.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String displayName,
        String token
) {}
```

**2.5.2.5. `UserProfileDto`**
```java
package com.myshelf.dto.response;

import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String displayName,
        String avatarUrl
) {}
```

### 2.6. Перечисления (enums)

#### 2.6.1. `Category`
```java
package com.myshelf.enums;

public enum Category {
    HEADWEAR,
    TOP,
    BOTTOM,
    SHOES,
    OUTERWEAR,
    ACCESSORIES
}
```

#### 2.6.2. `Season`
```java
package com.myshelf.enums;

public enum Season {
    WINTER,
    SPRING,
    SUMMER,
    AUTUMN,
    ALL_SEASONS
}
```