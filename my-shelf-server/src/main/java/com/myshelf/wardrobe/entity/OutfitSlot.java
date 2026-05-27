package com.myshelf.wardrobe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность слота образа.
 */
@Entity
@Table(name = "outfit_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitSlot {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "slot_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category slotType;
}
