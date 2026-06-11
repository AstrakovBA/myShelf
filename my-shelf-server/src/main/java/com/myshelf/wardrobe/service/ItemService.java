package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.mapper.ItemMapper;
import com.myshelf.wardrobe.repository.ItemRepository;
import com.myshelf.wardrobe.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
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

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    /**
     * Создаёт сервис вещей гардероба.
     *
     * @param itemRepository репозиторий вещей
     * @param userRepository репозиторий пользователей
     * @param itemMapper маппер Item ↔ DTO
     */
    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
    }

    /**
     * Создаёт новую вещь для пользователя.
     *
     * @param userId идентификатор владельца
     * @param itemDTO данные вещи
     * @return сохранённая вещь
     */
    public ItemDTO createItem(UUID userId, ItemDTO itemDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Item item = itemMapper.toEntity(itemDTO, user);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDTO(savedItem);
    }

    /**
     * Возвращает вещь по идентификатору.
     *
     * @param itemId идентификатор вещи
     * @return DTO вещи
     */
    @Transactional(readOnly = true)
    public ItemDTO getItemById(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена"));
        return itemMapper.toDTO(item);
    }

    /**
     * Возвращает все вещи пользователя.
     *
     * @param userId идентификатор владельца
     * @return список DTO вещей
     */
    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByUserId(UUID userId) {
        List<Item> items = itemRepository.findByUser_Id(userId);
        return items.stream()
                .map(itemMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет данные вещи.
     *
     * @param itemId идентификатор вещи
     * @param itemDTO новые данные
     * @return обновлённая вещь
     */
    public ItemDTO updateItem(UUID itemId, UUID currentUserId, ItemDTO itemDTO) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена"));

        if (!item.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Нет доступа к этой вещи");
        }

        itemMapper.updateEntityFromDTO(item, itemDTO);
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toDTO(updatedItem);
    }

    /**
     * Демонстрация паттерна <strong>Identity Map</strong> (карта идентичностей) в Hibernate.
     * <p>
     * Identity Map — это паттерн из книги <em>Patterns of Enterprise Application Architecture</em> (Martin Fowler).
     * Он гарантирует, что в рамках одного {@linkplain jakarta.persistence.PersistenceContext
     * Persistence Context} (контекста персистентности) для каждого первичного ключа существует
     * ровно один экземпляр сущности в памяти JVM. Повторная загрузка по тому же {@code id}
     * не создаёт новый объект: Hibernate возвращает уже управляемый (managed) экземпляр
     * из внутренней структуры {@code Map&lt;Identifier, Entity&gt;}.
     * <p>
     * В Spring Data JPA контекст персистентности привязан к транзакции: пока активна
     * аннотация {@code @Transactional} на этом методе, оба вызова {@code findById} используют
     * один и тот же {@code EntityManager}, поэтому {@code first == second} возвращает {@code true}.
     * <p>
     * После завершения транзакции контекст очищается, и следующая загрузка может вернуть
     * другой объект-обёртку (detached/new managed instance), даже при том же {@code id}.
     *
     * @param id идентификатор вещи
     * @return {@code true}, если обе загрузки вернули один и тот же объект в памяти ({@code ==})
     */
    @Transactional(readOnly = true)
    public boolean getItemWithIdentityCheck(UUID id) {
        Item first = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена"));
        Item second = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Вещь не найдена"));

        boolean sameInstance = first == second;
        log.info(
                "Identity Map: повторная загрузка Item id={} в одной транзакции — "
                        + "один объект в памяти (оператор ==): {}",
                id, sameInstance);

        return sameInstance;
    }

    /**
     * Удаляет вещь по идентификатору.
     *
     * @param itemId идентификатор вещи
     */
    public void deleteItem(UUID itemId, UUID currentUserId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена"));

        if (!item.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Нет доступа к этой вещи");
        }

        itemRepository.delete(item);
    }
}
