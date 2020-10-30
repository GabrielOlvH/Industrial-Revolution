package me.steven.indrev.api

import com.google.common.collect.Multimap
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack

interface AttributeModifierProvider {
    fun getAttributeModifiers(itemStack: ItemStack, equipmentSlot: EquipmentSlot): Multimap<EntityAttribute, EntityAttributeModifier>
}