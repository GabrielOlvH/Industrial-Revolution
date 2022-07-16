package me.steven.indrev.blockentities.miningrig

import com.google.common.base.Preconditions
import io.netty.buffer.Unpooled
import me.steven.indrev.api.OreDataCards
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.gui.screenhandlers.machines.MiningRigDrillScreenHandler
import me.steven.indrev.packets.client.MiningRigSpawnBlockParticlesPacket
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class DrillBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(IRBlockRegistry.DRILL_BLOCK_ENTITY_TYPE, pos, state), ExtendedScreenHandlerFactory {
    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    var position: Double = 1.0

    var miningProgress: Double = 0.0

    fun setWorkingState(working: Boolean) {
        if (cachedState[DrillBlock.WORKING] != working)
            world?.setBlockState(pos, cachedState.with(DrillBlock.WORKING, working))
    }

    fun tickMining(miningRig: MiningRigBlockEntity, data: OreDataCards.Data) {
        setWorkingState(true)
        miningProgress += getSpeedMultiplier()

        if (miningProgress >= data.speed) {
            miningProgress = 0.0
            val item = data.pickRandom(world!!.random)
            val rng = if (data.rng == 1 && world!!.random.nextDouble() < 0.1) 2 else 1
            val count = ((5 * data.richness) + world!!.random.nextInt((OreDataCards.MAX_PER_CYCLE * data.richness).toInt().coerceAtLeast(1))).toInt() * rng
            var stack = ItemStack(item, count)

            if (data.rng == 1 && world!!.random.nextDouble() > 0.9) {
                stack.count *= 2
            } else if (data.rng == -1 && world!!.random.nextDouble() > 0.9) {
                stack = ItemStack.EMPTY
            }

            miningRig.output(stack)

            data.used++
            miningRig.lastMinedItem = ItemStack(item)
            miningRig.sync()

            val drillStack = inventory[0]
            drillStack.damage(1, world!!.random, null)
            if (drillStack.damage >= drillStack.maxDamage) {
                inventory[0] = ItemStack.EMPTY
            }

            if (item is BlockItem)
                sendBlockBreakPacket(item.block)
        }
    }

    private fun sendBlockBreakPacket(block: Block) {
        val (x, y, z) = pos
        val players = (world as ServerWorld).server.playerManager.playerList
        for (i in players.indices) {
            val serverPlayerEntity = players[i]
            if (serverPlayerEntity.world.registryKey === world!!.registryKey) {
                val xOffset = x - serverPlayerEntity.x
                val yOffset = y - serverPlayerEntity.y
                val zOffset = z - serverPlayerEntity.z
                if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset < 64 * 64) {
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeBlockPos(pos)
                    buf.writeInt(Registry.BLOCK.getRawId(block))
                    ServerPlayNetworking.send(serverPlayerEntity, MiningRigSpawnBlockParticlesPacket.BLOCK_BREAK_PACKET, buf)
                }
            }
        }
    }

    override fun size(): Int = 1

    override fun getContainerName(): Text = translatable("block.indrev.drill")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return MiningRigDrillScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventory

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventory = list
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
        if (!deserializeLootTable(tag)) {
            Inventories.readNbt(tag, inventory)
        }
        position = tag.getDouble("Position")
        miningProgress = tag.getDouble("Mining")
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        if (!serializeLootTable(tag)) {
            Inventories.writeNbt(tag, inventory)
        }
        tag.putDouble("Position", position)
        tag.putDouble("Mining", miningProgress)
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        writeNbt(nbt)
        return nbt
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
            IRItemRegistry.STONE_DRILL_HEAD -> 1.0
            IRItemRegistry.IRON_DRILL_HEAD -> 2.0
            IRItemRegistry.DIAMOND_DRILL_HEAD -> 4.0
            IRItemRegistry.NETHERITE_DRILL_HEAD -> 8.0
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