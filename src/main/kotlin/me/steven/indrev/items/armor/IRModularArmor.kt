package me.steven.indrev.items.armor

import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.Module
import me.steven.indrev.items.rechargeable.Rechargeable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRModularArmor(slot: EquipmentSlot, settings: Settings) :
    DyeableArmorItem(IRArmorMaterial.MODULAR, slot, settings), Rechargeable {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        val upgrades = Module.getInstalled(stack)
        if (upgrades.isNotEmpty()) {
            tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade").formatted(Formatting.GOLD))
            upgrades.forEach { upgrade ->
                tooltip?.add(
                    TranslatableText(
                        "item.indrev.modular_armor.upgrade.${upgrade.key}",
                        Module.getLevel(stack, upgrade)
                    ).formatted(Formatting.BLUE)
                )
            }
        }
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getColor(stack: ItemStack?): Int {
        val compoundTag = stack!!.getSubTag("display")
        return if (compoundTag != null && compoundTag.contains("color", 99)) compoundTag.getInt("color") else -1
    }
}