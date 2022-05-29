package me.steven.indrev.blockentities.storage

import me.steven.indrev.gui.screenhandlers.storage.CabinetScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos

class CabinetBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(IRBlockRegistry.CABINET_BLOCK_ENTITY_TYPE, pos, state), ExtendedScreenHandlerFactory {

    private var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)

    override fun size(): Int = 27

    override fun getContainerName(): Text = translatable("block.indrev.cabinet")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return CabinetScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventory

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventory = list
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
        if (!deserializeLootTable(tag)) {
            Inventories.readNbt(tag, inventory)
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        if (!serializeLootTable(tag)) {
            Inventories.writeNbt(tag, inventory)
        }
    }
}