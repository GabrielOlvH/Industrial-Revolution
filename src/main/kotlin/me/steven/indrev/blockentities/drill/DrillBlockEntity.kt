package me.steven.indrev.blockentities.drill

import com.google.common.base.Preconditions
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.gui.screenhandlers.machines.MiningRigDrillScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DrillBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(IRBlockRegistry.DRILL_BLOCK_ENTITY_TYPE, pos, state), ExtendedScreenHandlerFactory {
    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    var position: Double = 1.0

    fun setWorkingState(working: Boolean) {
        if (cachedState[DrillBlock.WORKING] != working)
            world?.setBlockState(pos, cachedState.with(DrillBlock.WORKING, working))
    }

    override fun size(): Int = 1

    override fun getContainerName(): Text = TranslatableText("block.indrev.drill")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return MiningRigDrillScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventory

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventory = list
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
        if (!deserializeLootTable(tag)) {
            Inventories.readNbt(tag, inventory)
        }
        position = tag?.getDouble("Position") ?: position
    }

    override fun writeNbt(tag: NbtCompound?) {
        super.writeNbt(tag)
        if (!serializeLootTable(tag)) {
            Inventories.writeNbt(tag, inventory)
        }
        tag?.putDouble("Position", position)
    }

    fun sync() {
        Preconditions.checkNotNull(world) // Maintain distinct failure case from below
        check(world is ServerWorld) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        (world as ServerWorld).chunkManager.markForUpdate(getPos())
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    fun getSpeedMultiplier(): Double {
        val item = inventory[0].item
        return if (position > 0) 0.0 else when (item) {
            IRItemRegistry.STONE_DRILL_HEAD -> 0.5
            IRItemRegistry.IRON_DRILL_HEAD -> 2.0
            IRItemRegistry.DIAMOND_DRILL_HEAD -> 5.0
            IRItemRegistry.NETHERITE_DRILL_HEAD -> 10.0
            else -> 0.0
        }
    }

    companion object {
        fun isValidDrill(item: Item) =
            item == IRItemRegistry.STONE_DRILL_HEAD
                    || item == IRItemRegistry.IRON_DRILL_HEAD
                    || item == IRItemRegistry.DIAMOND_DRILL_HEAD
                    || item == IRItemRegistry.NETHERITE_DRILL_HEAD

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: DrillBlockEntity) {

            if (blockEntity.inventory[0].isEmpty) {
                blockEntity.position = 1.0
                blockEntity.markDirty()
                blockEntity.sync()
            } else if (state[DrillBlock.WORKING]) {
                if (blockEntity.position > 0) blockEntity.position -= 0.01

                else return
                blockEntity.markDirty()
                blockEntity.sync()
            }
        }
    }
}