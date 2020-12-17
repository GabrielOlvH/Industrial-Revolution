package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.ContainerItemContext
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.item.ItemKey
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.world.World

open class IRMiningDrill(
    toolMaterial: ToolMaterial,
    private val tier: Tier,
    private val maxStored: Double,
    val baseMiningSpeed: Float,
    settings: Settings
) : PickaxeItem(toolMaterial, 0, 0F, settings), IREnergyItem {

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val material = state?.material
        val hasEnergy = (EnergyApi.ITEM[ItemKey.of(stack), ContainerItemContext.ofStack(stack)]?.energy ?: 0.0) > 0
        return when {
            SUPPORTED_MATERIALS.contains(material) && hasEnergy -> baseMiningSpeed
            !hasEnergy -> 0F
            else -> super.getMiningSpeedMultiplier(stack, state)
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        buildEnergyTooltip(stack, tooltip)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val handler = EnergyApi.ITEM[ItemKey.of(stack), ContainerItemContext.ofStack(stack)] ?: return
        stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceIn(1, stack.maxDamage - 1)
    }

    companion object {
        val SUPPORTED_MATERIALS = arrayOf(
            Material.METAL,
            Material.STONE,
            Material.WOOD,
            Material.BAMBOO,
            Material.COBWEB,
            Material.PISTON,
            Material.GOURD,
            Material.SOIL,
            Material.SOLID_ORGANIC,
            Material.LEAVES,
            Material.AGGREGATE
        )
    }
}