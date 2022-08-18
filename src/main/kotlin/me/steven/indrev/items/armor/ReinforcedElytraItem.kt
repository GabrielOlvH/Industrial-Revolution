package me.steven.indrev.items.armor

import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ElytraItem
import net.minecraft.item.ItemStack

class ReinforcedElytraItem : ArmorItem(IRArmorMaterial.STEEL_ELYTRA, EquipmentSlot.CHEST, itemSettings().maxDamage(800)), FabricElytraItem {

    override fun useCustomElytra(entity: LivingEntity, chestStack: ItemStack, tickElytra: Boolean): Boolean {
        return canFallFly(chestStack) && super.useCustomElytra(entity, chestStack, tickElytra)
    }

    companion object {
        fun canFallFly(itemStack: ItemStack): Boolean {
            return (itemStack.isOf(IRItemRegistry.REINFORCED_ELYTRA) && ElytraItem.isUsable(itemStack))
                    || (itemStack.isOf(IRItemRegistry.MODULAR_ARMOR_CHEST)
                    && ArmorModule.ELYTRA.getLevel(itemStack) > 0
                    && energyOf(itemStack)!!.amount > 0)
        }

        fun hasValidElytra(itemStack: ItemStack): Boolean {
            return (itemStack.isOf(IRItemRegistry.REINFORCED_ELYTRA))
                    || (itemStack.isOf(IRItemRegistry.MODULAR_ARMOR_CHEST)
                    && IRItemRegistry.MODULAR_ARMOR_CHEST.getInstalled(itemStack).contains(ArmorModule.ELYTRA))
        }
    }
}