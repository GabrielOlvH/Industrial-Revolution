package me.steven.indrev.armor

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

enum class IRArmorMaterial(
    private val armorName: String,
    private val durabilityMultiplier: Int,
    private val armorValues: IntArray,
    private val enchantability: Int,
    private val equipSound: SoundEvent,
    private val toughness: Float,
    private val knockbackResistance: Float,
    private val repairIngredient: () -> Ingredient?) : ArmorMaterial {

    MODULAR(
        "modular", 0, intArrayOf(1, 3, 2, 1), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F, 1.0F, { null }
    ),
    STEEL_ELYTRA(
        "reinforced_elytra", 30, intArrayOf(1, 1, 1, 1), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F, 0F, { Ingredient.ofItems(IRItemRegistry.STEEL_PLATE()) }
    ),
    STEEL(
        "steel", 30, intArrayOf(2, 6, 7, 2), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F, 0F, { Ingredient.ofItems(IRItemRegistry.STEEL_INGOT()) }
    ),
    COPPER(
        "copper", 14, intArrayOf(2, 4, 5, 2), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, { Ingredient.ofItems(IRItemRegistry.COPPER_INGOT()) }
    ),
    TIN(
        "tin", 10, intArrayOf(1, 4, 5, 2), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, { Ingredient.ofItems(IRItemRegistry.TIN_INGOT()) }
    ),
    BRONZE(
        "bronze", 15, intArrayOf(2, 5, 6, 2), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, { Ingredient.ofItems(IRItemRegistry.BRONZE_INGOT()) }
    ),
    LEAD(
        "lead", 20, intArrayOf(2, 4, 4, 1), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, { Ingredient.ofItems(IRItemRegistry.LEAD_INGOT()) }
    ),
    SILVER(
        "silver", 30, intArrayOf(1, 4, 4, 1), 15,
        SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, { Ingredient.ofItems(IRItemRegistry.SILVER_INGOT()) }
    );

    override fun getName(): String = armorName
    override fun getEquipSound(): SoundEvent = equipSound
    override fun getRepairIngredient(): Ingredient? = repairIngredient()
    override fun getEnchantability(): Int = enchantability
    override fun getProtectionAmount(slot: EquipmentSlot): Int = this.armorValues[slot.entitySlotId]
    override fun getDurability(slot: EquipmentSlot): Int = BASE_DURABILITY[slot.entitySlotId] * durabilityMultiplier
    override fun getKnockbackResistance(): Float = knockbackResistance
    override fun getToughness(): Float = toughness

    companion object {
        private val BASE_DURABILITY = intArrayOf(13, 15, 16, 11)
    }
}