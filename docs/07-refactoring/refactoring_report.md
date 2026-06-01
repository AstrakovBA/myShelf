# Отчёт о рефакторинге my-shelf-server

---

## 1. Введение

### Цель рефакторинга

Целью рефакторинга модуля `my-shelf-server` было повысить **сопровождаемость**, **тестируемость** и **предсказуемость работы с данными** без изменения внешнего контракта REST API. Конкретные задачи:

- отделить доменные сущности от транспортного слоя (DTO);
- явно задокументировать и применить паттерны персистентности JPA/Hibernate;
- снизить риск `LazyInitializationException` и N+1 запросов;
- ввести автоматический контроль стиля кода (Checkstyle);
- расширить тестовое покрытие (unit + интеграционные JPA-тесты).

### Применяемые паттерны проектирования

| Паттерн | Область применения |
|---------|------------------|
| **Data Mapper** | Пакет `com.myshelf.wardrobe.mapper` — конвертация Entity ↔ DTO |
| **Identity Map** | Persistence Context Hibernate (демонстрация в `ItemService`, `IdentityMapTest`) |
| **Lazy Load** | Явные `FetchType`, `@EntityGraph`, `JOIN FETCH` в репозиториях |

Подробнее о паттернах персистентности: [patterns.md](patterns.md).

---

## 2. Реализованные паттерны

### 2.1 Data Mapper

#### Описание реализации

Введён отдельный пакет `com.myshelf.wardrobe.mapper` с Spring-компонентами:

| Класс | Назначение |
|-------|------------|
| `ItemMapper` | `Item` ↔ `ItemDTO`, обновление полей сущности |
| `OutfitMapper` | `Outfit` ↔ `OutfitDTO`, маппинг вложенных `OutfitSlot` |
| `UserMapper` | `User` ↔ `UserProfileDTO` |

Сервисы (`ItemService`, `OutfitService`, `AuthService`) больше не собирают сущности через `builder()` и не создают DTO вручную — вся конвертация сосредоточена в мапперах.

#### Примеры кода (до / после)

**До** (`ItemService.createItem` — фрагмент):

```java
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
return new ItemDTO(
        savedItem.getId(),
        savedItem.getName(),
        // ... остальные поля
);
```

**После:**

```java
Item item = itemMapper.toEntity(itemDTO, user);
Item savedItem = itemRepository.save(item);
return itemMapper.toDTO(savedItem);
```

**До** (`OutfitService` — создание слотов в цикле с дублированием `OutfitSlot.builder()`).

**После:**

```java
Outfit outfit = outfitMapper.toEntity(outfitDTO, user);
// ...
OutfitSlot slot = outfitMapper.toSlotEntity(slotDTO, outfit, item);
outfit.addSlot(slot);
```

#### Преимущества

- **Единая точка изменения** при добавлении полей в Entity/DTO.
- **Читаемость сервисов** — остаётся бизнес-логика и валидация.
- **Упрощение тестирования** — мапперы можно покрывать отдельными unit-тестами.
- **Соответствие PCMEF** — сущности не «протекают» в REST-слой.

---

### 2.2 Identity Map

#### Как работает в Hibernate

**Identity Map** реализуется через **Persistence Context**: в рамках одной транзакции для каждого первичного ключа существует не более одного managed-экземпляра сущности. Повторный `findById` возвращает ту же ссылку в JVM (`first == second`).

Контекст живёт, пока активна транзакция (`@Transactional`); после `commit`/`rollback` карта очищается.

#### Демонстрация через тесты

| Артефакт | Описание |
|----------|----------|
| `ItemService#getItemWithIdentityCheck(UUID)` | Два вызова `findById` в одной транзакции, лог результата `==` |
| `IdentityMapTest` | `@DataJpaTest` + H2: `assertSame` для `EntityManager.find` и `repository.findById` |

Пример из интеграционного теста:

```java
Item first = entityManager.find(Item.class, itemId);
Item second = entityManager.find(Item.class, itemId);
assertSame(first, second);
first.setName("Changed via first reference");
assertThat(second.getName()).isEqualTo("Changed via first reference");
```

Запуск: `mvn test -Dtest=IdentityMapTest` (профиль `test`, in-memory H2).

---

### 2.3 Lazy Load

#### Стратегии загрузки в JPA

| Связь | FetchType | Обоснование |
|-------|-----------|-------------|
| `User.items`, `User.outfits` | `LAZY` | Большие коллекции, не грузить при каждом `find(User)` |
| `Outfit.slots` | `EAGER` | Образ без слотов не используется в предметной области |
| `OutfitSlot.item` | `LAZY` | Опциональная вещь в слоте |
| `Item.user`, `Outfit.user` | `LAZY` | Владелец нужен выборочно |

#### Решение проблемы N+1

| Механизм | Применение |
|----------|------------|
| `@EntityGraph(attributePaths = {"user"})` | `ItemRepository.findByUser_Id(UUID)` — владелец вещи в одном запросе со списком |
| `LEFT JOIN FETCH` | `OutfitRepository.findWithSlots(UUID)` — образ со слотами |
| `@Transactional` в сервисе | Доступ к LAZY-полям внутри границы транзакции до маппинга в DTO |

`OutfitService.getOutfitDetails` использует `findWithSlots`, чтобы при вызове `outfitMapper.toDTO` коллекция `slots` была инициализирована и не возникала `LazyInitializationException`.

> **Примечание:** имя метода `findByUser_Id` выбрано вместо `findByUserIdWithUser`, т.к. Spring Data иначе ошибочно парсит суффикс `WithUser` как свойство (`Item.user.id.withUser`).

---

## 3. Метрики качества кода

### Покрытие тестами (JaCoCo)

Отчёт формируется при `mvn clean test` → `target/site/jacoco/index.html`.

**Сводка (последний прогон `mvn clean test`, 65 тестов):**

| Метрика | Покрыто | Пропущено | % |
|---------|---------|-----------|---|
| Инструкции | 1668 | 662 | **71,6%** |
| Строки | 368 | 124 | **74,8%** |

**Покрытие по слоям (инструкции, ориентир):**

| Пакет | Комментарий |
|-------|-------------|
| `service` | Высокое покрытие (`AuthService`, `OutfitService`, `ItemService`) |
| `mapper` | Покрытие через сервисные тесты |
| `controller` | Частично (`@WebMvcTest` для Item/Outfit; `AuthController` — без покрытия) |
| `security`, `config` | Низкое покрытие (JWT-фильтр, `SecurityConfig` не в slice-тестах) |
| `entity`, `dto` | В основном Lombok/геттеры — низкий приоритет для unit-тестов |

Команда: `mvn clean test` (JaCoCo подключается через `jacoco-maven-plugin` в `pom.xml`).

### Нарушения Checkstyle / SonarQube

#### Checkstyle

| Показатель | Значение |
|------------|----------|
| Конфигурация | `my-shelf-server/checkstyle.xml` (на базе Google Checks) |
| Профиль Maven | `-Pcheckstyle` |
| Отчёт | `target/checkstyle-result.xml` |
| Нарушений (текущая линия) | **0** |
| Нарушений до рефакторинга | 37 (JavaDoc, star-import, `catch (Exception)`, …) |

Команда: `mvn checkstyle:check` из каталога `my-shelf-server`.

#### SonarQube

Интеграция с SonarQube в репозитории **не настроена**. В README корневого проекта SonarQube указан как планируемый инструмент. Рекомендуется подключить `sonar-maven-plugin` на этапе CI для дублирования части правил Checkstyle и отслеживания code smells.

### Цикломатическая сложность

| Источник | Ограничение | Фактическое состояние |
|----------|-------------|------------------------|
| Checkstyle `CyclomaticComplexity` | max **10** на метод | Нарушений не выявлено |
| Наиболее сложные методы | — | `OutfitService.applySlotsFromDTO` / `resolveSlotItem` (валидация слотов), `AuthService.register` |

Рефакторинг вынес маппинг и повторяющееся создание слотов в `OutfitMapper`, что удерживает сложность сервисных методов в приемлемых пределах.

---

## 4. Рефакторинги кода

### Улучшения в Service слое

- Внедрены **Data Mapper** (`ItemMapper`, `OutfitMapper`, `UserMapper`).
- `ItemService.getItemsByUserId` — загрузка через `findByUser_Id` с Entity Graph.
- `OutfitService.getOutfitDetails` / `updateOutfit` — `findWithSlots` вместо «голого» `findById`.
- Валидация слотов образа вынесена в `applySlotsFromDTO` и `resolveSlotItem`.
- Добавлен `getItemWithIdentityCheck` для демонстрации Identity Map.
- Добавлены **JavaDoc** для публичных методов и конструкторов (требование Checkstyle).

### Улучшения в Repository слое

- `ItemRepository.findByUser_Id` + `@EntityGraph(attributePaths = {"user"})`.
- `OutfitRepository.findWithSlots` — JPQL `LEFT JOIN FETCH o.slots`.
- Исправлено имя метода репозитория (устранена ошибка контекста Spring Data в `IdentityMapTest`).

### Улучшения в тестах

| Изменение | Назначение |
|-----------|------------|
| `IdentityMapTest` | Интеграционная проверка Identity Map (`@DataJpaTest`, H2) |
| `application-test.properties` | In-memory БД, `ddl-auto=create-drop`, Flyway отключён |
| Обновлены моки в `ItemServiceTest`, `OutfitServiceTest` | `findByUser_Id`, `findWithSlots` |
| `@Spy` реальных мапперов в unit-тестах сервисов | Корректная работа `@InjectMocks` |

**Итого тестов:** 65 (0 failures, 0 errors) при `mvn clean test`.

---

## 5. Заключение

### Достигнутые результаты

1. Выделен слой **Data Mapper** — сервисы не зависят от деталей сборки Entity/DTO.
2. Документированы и продемонстрированы **Identity Map** и **Lazy Load** ([patterns.md](patterns.md), тесты).
3. Снижен риск **N+1** и `LazyInitializationException` для сценариев списка вещей и деталей образа.
4. Настроен **Checkstyle** с нулевым числом нарушений на текущей кодовой базе.
5. Покрытие JaCoCo: **~72%** инструкций, **~75%** строк; все **65** тестов проходят.

### Планы на будущее

| Направление | Действие |
|-------------|----------|
| Покрытие | Unit-тесты для `*Mapper`; `@WebMvcTest` для `AuthController` |
| Безопасность | Тесты `JwtAuthFilter`, `JwtTokenProvider` |
| SonarQube | Подключение анализа в CI/CD |
| MapStruct | Опциональная замена ручных мапперов при росте числа DTO |
| Репозитории | Унификация имён запросов (`findByUser_Id` для `Outfit`, `existsByIdAndUser_Id`) |
| Производительность | `@EntityGraph` / batch-fetch для `OutfitSlot.item` при списке образов |

---

## Связанные документы

- [patterns.md](patterns.md) — Identity Map, Lazy Load, EntityGraph
- [ORM-strategy.md](../03-database/ORM-strategy.md) — общая ORM-стратегия
- [README этапа 6 — тестирование](../06-testing/README.md) — JaCoCo, Checkstyle
- [my-shelf-server/README.md](../../my-shelf-server/README.md) — Code Quality, запуск Checkstyle
