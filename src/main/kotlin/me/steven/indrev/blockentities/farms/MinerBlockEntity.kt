package me.steven.indrev.blockentities.farms

import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.IRConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.misc.IRResourceReportItem
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinState
import me.steven.indrev.world.chunkveins.VeinType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry

class MinerBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MINER_REGISTRY), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancementsSlots: IntArray = intArrayOf(10, 11, 12, 13)
    override val availableEnhancers: Array<Enhancer> = arrayOf(Enhancer.BUFFER, Enhancer.ENERGY)

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

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    private var chunkVeinType: VeinType? = null
    private var mining = 0.0
    private var finished = false
    var lastMinedItem: ItemStack = ItemStack.EMPTY
    var requiredPower = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cacheVeinType()
        val enhancements = getEnhancers(inventory)
        requiredPower = Enhancer.getEnergyCost(enhancements, this)
        if (finished) {
            workingState = false
            getActiveDrills().forEach { drill -> drill.setWorkingState(false) }
            return
        } else if (isLocationCorrect() && use(requiredPower)) {
            workingState = true
            getActiveDrills().forEach { drill -> drill.setWorkingState(true) }
            mining += Enhancer.getSpeed(enhancements, this)
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
        }
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
                    ServerPlayNetworking.send(serverPlayerEntity, BLOCK_BREAK_PACKET, buf)
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
        return VALID_DRILL_POSITIONS.map { pos.add(it) }.mapNotNull { pos ->
            val blockState = world?.getBlockState(pos)
            val block = blockState?.block
            if (block is DrillBlock) {
                val blockEntity = world?.getBlockEntity(block.part.getBlockEntityPos(pos)) as? DrillBlockEntity ?: return@mapNotNull null
                val itemStack = blockEntity.inventory[0]
                if (!itemStack.isEmpty && DrillBlockEntity.isValidDrill(itemStack.item)) {
                    blockEntity
                } else {
                    blockEntity.setWorkingState(false)
                    null
                }
            } else null
        }
    }

    override fun getBaseValue(enhancer: Enhancer): Double {
        val activeDrills = getActiveDrills()
        return when (enhancer) {
            Enhancer.ENERGY -> config.energyCost + (IRConfig.machines.drill * activeDrills.size)
            Enhancer.SPEED -> activeDrills.sumByDouble { blockEntity ->
                blockEntity.inventory[0]
                blockEntity.getSpeedMultiplier()
            }
            Enhancer.BUFFER -> config.maxEnergyStored
            else -> 0.0
        }
    }

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        val direction = (state.block as MachineBlock).getFacing(state)
        when (type) {
            ConfigurationType.ITEM -> {
                configuration[direction.rotateYCounterclockwise()] = TransferMode.OUTPUT
            }
            else -> super.applyDefault(state, type, configuration)
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> arrayOf(TransferMode.OUTPUT, TransferMode.NONE)
            else -> return super.getValidConfigurations(type)
        }
    }

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
        tag?.put("LastMinedBlock", lastMinedItem.toTag(CompoundTag()))
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("VeinIdentifier") == true && !tag.getString("VeinIdentifier").isNullOrEmpty())
            chunkVeinType = VeinType.REGISTERED[Identifier(tag.getString("VeinIdentifier"))]
        lastMinedItem = ItemStack.fromTag(tag?.getCompound("LastMinedBlock"))
        super.fromClientTag(tag)
    }

    companion object {
        val BLOCK_BREAK_PACKET = identifier("miner_drill_block_particle")

        val VALID_DRILL_POSITIONS = arrayOf(
            BlockPos(-1, 0, 0),
            BlockPos(1, 0, 0),
            BlockPos(0, 0, -1),
            BlockPos(0, 0, 1),
            BlockPos(-1, 0, -1),
            BlockPos(-1, 0, 1),
            BlockPos(1, 0, 1),
            BlockPos(1, 0, -1)
        )
    }
}