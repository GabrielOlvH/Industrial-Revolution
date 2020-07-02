package me.steven.indrev.items.rechargeable

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class IRMiningDrill(toolMaterial: ToolMaterial, settings: Settings) : PickaxeItem(toolMaterial, 0, 0F, settings), Rechargeable {
    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val material = state?.material
        return if (SUPPORTED_MATERIALS.contains(material) && stack.maxDamage - stack.damage > 1) this.material.miningSpeedMultiplier * 2 else 0F
    }

    override fun postMine(stack: ItemStack, world: World?, state: BlockState?, pos: BlockPos?, miner: LivingEntity?): Boolean {
        if (stack.damage + 1 > stack.maxDamage) return false
        return super.postMine(stack, world, state, pos, miner)
    }

    override fun canMine(state: BlockState?, world: World?, pos: BlockPos?, miner: PlayerEntity?): Boolean {
        val stack = miner?.mainHandStack ?: return super.canMine(state, world, pos, miner)
        return super.canMine(state, world, pos, miner) && stack.damage + 1 < stack.maxDamage
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        tooltip?.add(LiteralText("${if (stack?.isDamaged == true) (stack.maxDamage - stack.damage) else stack?.maxDamage} / ${stack?.maxDamage} LF"))
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