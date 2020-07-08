package me.steven.indrev.items.armor

import me.steven.indrev.armor.ModularArmorMaterial
import me.steven.indrev.armor.ModularUpgrade
import me.steven.indrev.items.rechargeable.Rechargeable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRModularArmor(slot: EquipmentSlot, settings: Settings) : ArmorItem(ModularArmorMaterial.MODULAR, slot, settings), Rechargeable {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        val upgrades = ModularUpgrade.getUpgrades(stack)
        if (upgrades.isNotEmpty()) {
            tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade").formatted(Formatting.GOLD))
            upgrades.forEach { upgrade ->
                tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade.${upgrade.key}", ModularUpgrade.getLevel(stack, upgrade)).formatted(Formatting.BLUE))
            }
        }
        super.appendTooltip(stack, world, tooltip, context)
    }
}