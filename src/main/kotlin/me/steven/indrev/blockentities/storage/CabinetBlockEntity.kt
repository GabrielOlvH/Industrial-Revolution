package me.steven.indrev.blockentities.storage

import me.steven.indrev.gui.screenhandlers.storage.CabinetScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList

class CabinetBlockEntity : LootableContainerBlockEntity(IRBlockRegistry.CABINET_BLOCK_ENTITY_TYPE), ExtendedScreenHandlerFactory {

    private var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)

    override fun size(): Int = 27

    override fun getContainerName(): Text = TranslatableText("block.indrev.cabinet")

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

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
        if (!deserializeLootTable(tag)) {
            Inventories.fromTag(tag, inventory)
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag? {
        super.toTag(tag)
        if (!serializeLootTable(tag)) {
            Inventories.toTag(tag, inventory)
        }
        return tag
    }
}