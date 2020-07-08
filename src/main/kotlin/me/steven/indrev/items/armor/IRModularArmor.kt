package me.steven.indrev.items.armor

import me.steven.indrev.armor.ModularArmorMaterial
import me.steven.indrev.items.rechargeable.Rechargeable
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem

class IRModularArmor(slot: EquipmentSlot, settings: Settings) : ArmorItem(ModularArmorMaterial.MODULAR, slot, settings), Rechargeable