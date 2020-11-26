package me.steven.indrev.blockentities.farms

import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.misc.IRResourceReportItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinState
import me.steven.indrev.world.chunkveins.VeinType
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class MinerBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MINER_REGISTRY), UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryComponent = inventory(this) {
            input {
                slot = 14
                filter { itemStack, _ -> itemStack.item is IRResourceReportItem }
            }
            output { slots = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9) }
        }
    }

    private var chunkVeinType: VeinType? = null
    private var mining = 0.0
    private var finished = false
    var lastMinedItem = ItemStack.EMPTY
    var requiredPower = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cacheVeinType()
        val upgrades = getUpgrades(inventory)
        requiredPower = Upgrade.getEnergyCost(upgrades, this)
        if (finished) {
            workingState = false
            getActiveDrills().forEach { drill -> drill.setWorkingState(false) }
            return
        } else if (isLocationCorrect() && Energy.of(this).use(requiredPower)) {
            getActiveDrills().forEach { drill -> drill.setWorkingState(true) }
            mining += Upgrade.getSpeed(upgrades, this)
            temperatureComponent?.tick(true)
        } else {
            workingState = false
            getActiveDrills().forEach { drill -> drill.setWorkingState(false) }
            temperatureComponent?.tick(false)
        }
        if (mining >= config.processSpeed) {
            updateData { data ->
                if (data == null) {
                    chunkVeinType = null
                    return@updateData false
                }
                val (_, size, explored) = data
                propertyDelegate[3] = explored * 100 / size
                if (explored >= size) {
                    finished = true
                    return
                }
                data.explored++
                mining = 0.0
                val generatedOre = chunkVeinType!!.outputs.pickRandom(world?.random)
                lastMinedItem = ItemStack(generatedOre)
                inventory.output(lastMinedItem.copy())
                sync()
                getActiveDrills().filter { d -> d.getSpeedMultiplier() > 0 }.random().also { drillBlockEntity ->
                    val itemStack = drillBlockEntity.inventory[0]
                    if (!itemStack.isEmpty) {
                        val speed = drillBlockEntity.getSpeedMultiplier()
                        if (speed > 0) {
                            itemStack.damage++
                            if (itemStack.damage >= itemStack.maxDamage) {
                                itemStack.decrement(1)
                            }
                            drillBlockEntity.markDirty()
                            if (itemStack.isEmpty)
                                drillBlockEntity.sync()
                        }
                    }

                    sendBlockBreakPacket(drillBlockEntity.pos, generatedOre)
                }
                workingState = true
                return@updateData true
            }
        } else workingState = false
    }

    private fun sendBlockBreakPacket(pos: BlockPos, block: Block) {
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
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(serverPlayerEntity, BLOCK_BREAK_PACKET, buf)
                }
            }
        }
    }

    private fun isLocationCorrect(): Boolean {
        val inventory = inventoryComponent?.inventory ?: return false
        val scanOutput = inventory.getStack(14).tag ?: return false
        val scanChunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
        val chunkPos = world?.getChunk(pos)?.pos ?: return false
        return chunkPos == scanChunkPos
    }

    private fun cacheVeinType() {
        if (chunkVeinType == null) {
            updateData { data ->
                if (data == null) return@updateData false
                this.chunkVeinType = VeinType.REGISTERED[data.veinIdentifier]
                propertyDelegate[3] = data.explored * 100 / data.size
                if (data.explored >= data.size) {
                    finished = true
                }
                return@updateData false
            }
        }
    }

    private inline fun updateData(action: (ChunkVeinData?) -> Boolean) {
        val inventory = inventoryComponent?.inventory ?: return
        val scanOutput = inventory.getStack(14).tag ?: return
        val chunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
        val state =
            (world as ServerWorld).persistentStateManager.getOrCreate(
                { ChunkVeinState(ChunkVeinState.STATE_OVERWORLD_KEY) },
                ChunkVeinState.STATE_OVERWORLD_KEY
            )
        if (action(state.veins[chunkPos])) state.markDirty()
    }

    fun getActiveDrills(): List<DrillBlockEntity> {
        val offsets = arrayOf(BlockPos(-1, 0, -1), BlockPos(-1, 0, 1), BlockPos(1, 0, 1), BlockPos(1, 0, -1))
        return offsets.map { pos.add(it) }.mapNotNull { pos ->
            val blockState = world?.getBlockState(pos)
            val block = blockState?.block
            if (block is DrillBlock) {
                val blockEntity = world?.getBlockEntity(block.part.getBlockEntityPos(pos)) as? DrillBlockEntity ?: return@mapNotNull null
                val itemStack = blockEntity.inventory[0]
                if (!itemStack.isEmpty && DrillBlockEntity.isValidDrill(itemStack.item)) {
                    blockEntity
                }
                else {
                    blockEntity.setWorkingState(false)
                    null
                }
            } else null
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(10, 11, 12, 13)

    override fun getAvailableUpgrades(): Array<Upgrade> = arrayOf(Upgrade.BUFFER, Upgrade.ENERGY)

    override fun getBaseValue(upgrade: Upgrade): Double {
        val activeDrills = getActiveDrills()
        return when (upgrade) {
            Upgrade.ENERGY -> config.energyCost + (IndustrialRevolution.CONFIG.machines.drill * activeDrills.size)
            Upgrade.SPEED -> activeDrills.sumByDouble { blockEntity ->
                blockEntity.inventory[0]
                blockEntity.getSpeedMultiplier()
            }
            Upgrade.BUFFER -> getBaseBuffer()
            else -> 0.0
        }
    }

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag?.putString("VeinIdentifier", chunkVeinType?.id.toString())
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("VeinIdentifier") == true && !tag.getString("VeinIdentifier").isNullOrEmpty())
            chunkVeinType = VeinType.REGISTERED[Identifier(tag.getString("VeinIdentifier"))]
        super.fromTag(state, tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag?.putString("VeinIdentifier", chunkVeinType?.id.toString())
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("VeinIdentifier") == true && !tag.getString("VeinIdentifier").isNullOrEmpty())
            chunkVeinType = VeinType.REGISTERED[Identifier(tag.getString("VeinIdentifier"))]
        super.fromClientTag(tag)
    }

    companion object {
        val BLOCK_BREAK_PACKET = identifier("miner_drill_block_particle")
    }
}