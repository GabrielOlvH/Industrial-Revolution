package me.steven.indrev.items.rechargeable

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRMiningDrill(toolMaterial: ToolMaterial, settings: Settings) : PickaxeItem(toolMaterial, 0, 0F, settings), Rechargeable {
    override fun getMiningSpeedMultiplier(stack: ItemStack?, state: BlockState?): Float {
        val material = state?.material
        return if (SUPPORTED_MATERIALS.contains(material) && stack?.damage ?: 0 >= 0) this.material.miningSpeedMultiplier * 2 else 0F
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        tooltip?.add(LiteralText("${if (stack?.isDamaged == true) stack.damage else stack?.maxDamage} / ${stack?.maxDamage} LF"))
        tooltip?.add(TranslatableText("item.indrev.rechargeable.tooltip").formatted(Formatting.ITALIC, Formatting.GRAY))
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    companion object {
        private val SUPPORTED_MATERIALS = arrayOf(
            Material.METAL,
            Material.STONE,
            Material.WOOD,
            Material.BAMBOO,
            Material.COBWEB,
            Material.PISTON,
            Material.GOURD,
            Material.SOIL
        )
    }
}