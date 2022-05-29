package me.steven.indrev.tools.modular

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

enum class DrillModule(
    override val key: String,
    override val maxLevel: Int,
    override val item: ItemConvertible
) : Module {
    RANGE("range", 5, { IRItemRegistry.RANGE_MODULE_ITEM }),
    FORTUNE("fortune", 3, { IRItemRegistry.FORTUNE_MODULE_ITEM }),
    SILK_TOUCH("silk_touch", 1, { IRItemRegistry.SILK_TOUCH_MODULE_ITEM }),
    CONTROLLED_DESTRUCTION("controlled_destruction", 1, { IRItemRegistry.CONTROLLED_DESTRUCTION_MODULE_ITEM }),
    MATTER_PROJECTOR("matter_projector", 1, { IRItemRegistry.MATTER_PROJECTOR_MODULE_ITEM });

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(translatable("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(translatable("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(RANGE, FORTUNE, SILK_TOUCH, MiningToolModule.EFFICIENCY, CONTROLLED_DESTRUCTION, MATTER_PROJECTOR)

        fun getBlacklistedPositions(stack: ItemStack): List<BlockPos> {
            val nbt = stack.nbt ?: return emptyList()
            if (
                !nbt.contains("BlacklistedPositions")
                || (CONTROLLED_DESTRUCTION.getLevel(stack) <= 0 && MATTER_PROJECTOR.getLevel(stack) <= 0)
                || RANGE.getLevel(stack) <= 0
            ) return emptyList()
            val list = nbt.getList("BlacklistedPositions", 10)
            return list.map { element -> NbtHelper.toBlockPos(element as NbtCompound) }
        }
    }
}