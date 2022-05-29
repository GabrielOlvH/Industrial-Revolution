package me.steven.indrev.tools.modular

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting

enum class MiningToolModule(
    override val key: String,
    override val maxLevel: Int,
    override val item: ItemConvertible
) : Module {
    EFFICIENCY("efficiency", 5, { IRItemRegistry.EFFICIENCY_MODULE_ITEM });

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(translatable("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(translatable("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
        tooltip?.add(translatable("item.indrev.module_parts_gamer_axe").formatted(Formatting.GOLD))
    }
}