package me.steven.indrev.items.armor

import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.Module
import me.steven.indrev.items.rechargeable.Rechargeable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRArmor(material: IRArmorMaterial, slot: EquipmentSlot, settings: Settings) : ArmorItem(material, slot, settings), Rechargeable {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        if (material == IRArmorMaterial.MODULAR) {
            val upgrades = Module.getUpgrades(stack)
            if (upgrades.isNotEmpty()) {
                tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade").formatted(Formatting.GOLD))
                upgrades.forEach { upgrade ->
                    tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade.${upgrade.key}", Module.getLevel(stack, upgrade)).formatted(Formatting.BLUE))
                }
            }
        }
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = material != IRArmorMaterial.MODULAR && super.canRepair(stack, ingredient)
}