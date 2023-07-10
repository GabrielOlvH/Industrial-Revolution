package me.steven.indrev.blocks.misc

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class NikoliteOreBlock(settings: Settings) : Block(settings) {
    override fun onStacksDropped(
        state: BlockState?,
        world: ServerWorld,
        pos: BlockPos?,
        tool: ItemStack?,
        dropExperience: Boolean
    ) {
        super.onStacksDropped(state, world, pos, tool, dropExperience)
        if (dropExperience && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
            val i = 1 + world.random.nextInt(5)
            dropExperience(world, pos, i)
        }
    }
}