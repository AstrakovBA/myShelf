package com.myshelf.myshelf_app.presentation.outfit

import com.myshelf.myshelf_app.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests outfit constructor form logic ([OutfitFormState]).
 * UI state is managed in the screen; validation and slot updates live in the form state.
 */
class OutfitConstructorViewModelTest {

    @Test
    fun `createOutfit validates at least one slot is filled`() {
        val emptySlotsState = OutfitFormState(
            name = "Summer look",
            slots = OutfitFormState.defaultEmptySlots()
        )

        val validated = emptySlotsState.validate(
            nameRequired = "Name required",
            slotsRequired = "Select at least one item"
        )

        assertFalse(validated.isValid)
        assertNull(validated.nameError)
        assertEquals("Select at least one item", validated.slotsError)
    }

    @Test
    fun `createOutfit validation fails when name is empty`() {
        val state = OutfitFormState(
            name = "",
            slots = OutfitFormState.defaultEmptySlots() + (Category.TOP to "item-1")
        )

        val validated = state.validate("Name required", "Select at least one item")

        assertFalse(validated.isValid)
        assertEquals("Name required", validated.nameError)
        assertNull(validated.slotsError)
    }

    @Test
    fun `selectItemForSlot updates slot with selected item`() {
        val initial = OutfitFormState()
        val itemId = "item-shirt-42"

        val updated = initial.selectItemForSlot(Category.TOP, itemId)

        assertEquals(itemId, updated.slots[Category.TOP])
        assertNull(updated.slots[Category.BOTTOM])
    }

    @Test
    fun `isValid is true when name and at least one slot are filled`() {
        val state = OutfitFormState(
            name = "Office outfit",
            slots = OutfitFormState.defaultEmptySlots() + (Category.SHOES to "shoe-1")
        )

        assertTrue(state.isValid)
    }

    private fun OutfitFormState.selectItemForSlot(
        slotType: SlotType,
        itemId: String
    ): OutfitFormState = copy(slots = slots + (slotType to itemId))
}
