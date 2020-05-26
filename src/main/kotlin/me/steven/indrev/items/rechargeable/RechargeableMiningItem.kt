package me.steven.indrev.items.rechargeable

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterials

class RechargeableMiningItem(settings: Settings) : PickaxeItem(ToolMaterials.DIAMOND, 0, 0F, settings), Rechargeable {
    override fun getMiningSpeed(stack: ItemStack?, state: BlockState?): Float {
        val material = state?.material
        return if (SUPPORTED_MATERIALS.contains(material)) 16F else this.material.miningSpeed
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    companion object {
        private val SUPPORTED_MATERIALS = arrayOf(
                Material.METAL,
                Material.ANVIL,
                Material.STONE,
                Material.EARTH,
                Material.ORGANIC,
                Material.WOOD,
                Material.BAMBOO,
                Material.CLAY,
                Material.COBWEB,
                Material.PUMPKIN,
                Material.PISTON
        )
    }
}