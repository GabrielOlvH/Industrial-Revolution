package me.steven.indrev.items.rechargeable

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterials
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class RechargeableMiningItem(settings: Settings) : PickaxeItem(ToolMaterials.DIAMOND, 0, 0F, settings), Rechargeable {
    override fun getMiningSpeedMultiplier(stack: ItemStack?, state: BlockState?): Float {
        val material = state?.material
        return if (SUPPORTED_MATERIALS.contains(material)) 16F else this.material.miningSpeedMultiplier
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip?.add(TranslatableText("gui.widget.energy"))
        tooltip?.add(LiteralText("${stack?.damage} / ${stack?.maxDamage} LF"))
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