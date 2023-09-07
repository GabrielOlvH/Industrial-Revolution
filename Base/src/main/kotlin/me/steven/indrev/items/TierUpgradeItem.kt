package me.steven.indrev.items

import me.steven.indrev.api.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MACHINES
import me.steven.indrev.blocks.Machine
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.utils.itemSettings
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

class TierUpgradeItem(val from: Tier, val to: Tier) : Item(itemSettings()) {
    fun canUse(blockEntity: MachineBlockEntity<*>, machine: Machine): Boolean {
        return from == blockEntity.tier && machine.tiers.contains(to)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient && entity is PlayerEntity && selected) {
            val target = MinecraftClient.getInstance().crosshairTarget
            if (target is BlockHitResult) {
                val blockState = world.getBlockState(target.blockPos)
                val block = blockState.block
                val blockEntity = world.getBlockEntity(target.blockPos)
                if (block is MachineBlock && blockEntity is MachineBlockEntity<*> && canUse(blockEntity, MACHINES[block.id]!!)) {
                    val blockItems = MACHINES[block.id]!!.blockItems
                    val fromText = Text.translatable(blockItems[from.ordinal].translationKey).styled { s -> s.withColor(from.color) }
                    val toText = Text.translatable(blockItems[to.ordinal].translationKey).styled { s -> s.withColor(to.color) }
                    entity.sendMessage(Text.literal("Click to upgrade ").append(fromText).append(Text.literal(" to ")).append(toText).formatted(Formatting.GREEN), true)
                }
            }
        }
    }
}