package me.steven.indrev.armor

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Lazy

enum class ModularArmorMaterial(
    private val armorName: String,
    private val durabilityMultiplier: Int,
    private val armorValues: IntArray,
    private val enchantability: Int,
    private val equipSound: SoundEvent,
    private val toughness: Float,
    private val knockbackResistance: Float,
    private val repairIngredient: Lazy<Ingredient>) : ArmorMaterial {

    MODULAR("modular", 5, intArrayOf(1, 3, 2, 1), 15, SoundEvents.BLOCK_WOOL_PLACE, 0.0F, 1.0F, Lazy<Ingredient> {
        Ingredient.ofItems(net.minecraft.item.Items.WHITE_WOOL)
    });

    override fun getName(): String = armorName
    override fun getEquipSound(): SoundEvent = equipSound
    override fun getRepairIngredient(): Ingredient = repairIngredient.get()
    override fun getEnchantability(): Int = enchantability
    override fun getProtectionAmount(slot: EquipmentSlot): Int = this.armorValues[slot.entitySlotId]
    override fun getDurability(slot: EquipmentSlot): Int = BASE_DURABILITY[slot.entitySlotId] * durabilityMultiplier
    override fun getKnockbackResistance(): Float = knockbackResistance
    override fun getToughness(): Float = toughness

    companion object {
        private val BASE_DURABILITY = intArrayOf(13, 15, 16, 11)
    }
}