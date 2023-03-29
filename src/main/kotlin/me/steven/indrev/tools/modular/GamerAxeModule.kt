package me.steven.indrev.tools.modular

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting

enum class GamerAxeModule(
    override val key: String,
    override val maxLevel: Int,
    override val item: ItemConvertible
) : Module {
    LOOTING("looting", 3, { IRItemRegistry.LOOTING_MODULE_ITEM }),
    FIRE_ASPECT("fire_aspect", 1, { IRItemRegistry.FIRE_ASPECT_MODULE_ITEM }),
    SHARPNESS("sharpness", 5, { IRItemRegistry.SHARPNESS_MODULE_ITEM }),
    REACH("reach", 4, { IRItemRegistry.SHARPNESS_MODULE_ITEM }); // NOT IMPLEMENTED

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(translatable("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(translatable("item.indrev.module_parts_gamer_axe").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(LOOTING, FIRE_ASPECT, SHARPNESS, REACH, MiningToolModule.EFFICIENCY)
    }
}