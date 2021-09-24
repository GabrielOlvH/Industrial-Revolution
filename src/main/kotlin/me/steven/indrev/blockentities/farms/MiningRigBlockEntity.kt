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
import me.steven.indrev.components.trackBoolean
import me.steven.indrev.components.trackDouble
import me.steven.indrev.components.trackInt
import me.steven.indrev.components.trackLong
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.IRConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.misc.IRResourceReportItem
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.packets.client.MiningRigSpawnBlockParticlesPacket
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinState
import me.steven.indrev.world.chunkveins.VeinType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey

class MiningRigBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MINING_RIG_REGISTRY, pos, state), BlockEntityClientSerializable, EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancerSlots: IntArray = intArrayOf(10, 11, 12, 13)
    override val availableEnhancers: Array<Enhancer> = arrayOf(Enhancer.BUFFER)

    init {
        this.inventoryComponent = inventory(this) {
            input {
                slot = 14
                filter { itemStack, _ -> itemStack.item is IRResourceReportItem }
            }
            output { slots = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9) }
        }
        trackInt(EXPLORED_PERCENTAGE_ID) {
            val inventory = inventoryComponent?.inventory ?: return@trackInt 0
            val scanOutput = inventory.getStack(14).nbt ?: return@trackInt 0
            val chunkPos = getChunkPos(scanOutput.getCompound("ChunkPos"))
            val state = ChunkVeinState.getState(world as ServerWorld)
            val data = state.veins[chunkPos] ?: return@trackInt 0
            data.explored * 100 / data.size
        }

        trackLong(ENERGY_REQUIRED_ID) { getEnergyCost() }
        trackBoolean(LOCATION_ID) { !hasReport() || isLocationCorrect() }
    }

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    private var chunkVeinType: VeinType? = null
    private var mining = 0.0
    private var finished = false
    var lastMinedItem: ItemStack = ItemStack.EMPTY

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cacheVeinType()

        val upgrades = getEnhancers()

        if (isLocationCorrect() && use(getEnergyCost())) {
            workingState = true
            getActiveDrills().forEach { drill -> drill.setWorkingState(true) }
            mining += Enhancer.getSpeed(upgrades, this)
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
                if (explored >= size) {
                    finished = true
                }
                else
                    data.explored++
                mining = 0.0

                val generatedOre : Block
                if (finished) {
                    generatedOre = chunkVeinType!!.infiniteOutputs.pickRandom()
                } else {
                    generatedOre = chunkVeinType!!.outputs.pickRandom()
                }

                lastMinedItem = ItemStack(generatedOre)
                inventory.output(lastMinedItem.copy())
                sync()
                getActiveDrills().filter { d -> d.getSpeedMultiplier() > 0 }.random().also { drillBlockEntity ->
                    val itemStack = drillBlockEntity.inventory[0]
                    if (!itemStack.isEmpty) {
                        val speed = drillBlockEntity.getSpeedMultiplier()
                        if (speed > 0) {
                            val damaged = itemStack.damage(1, world!!.random, null)
                            if (damaged && itemStack.damage >= itemStack.maxDamage) {
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
                    ServerPlayNetworking.send(serverPlayerEntity, MiningRigSpawnBlockParticlesPacket.BLOCK_BREAK_PACKET, buf)
                }
            }
        }
    }

    private fun hasReport(): Boolean {
        val inventory = inventoryComponent?.inventory ?: return false
        return !inventory.getStack(14).isEmpty
    }

    private fun isLocationCorrect(): Boolean {
        val inventory = inventoryComponent?.inventory ?: return false
        val scanOutput = inventory.getStack(14).nbt ?: return false
        val scanChunkPos = getChunkPos(scanOutput.getCompound("ChunkPos"))
        val chunkPos = world?.getChunk(pos)?.pos ?: return false
        val dimId = Identifier(scanOutput.getString("Dimension"))
        val key = RegistryKey.of(Registry.WORLD_KEY, dimId)
        val w = world!!.server!!.getWorld(key)
        return chunkPos == scanChunkPos && w == world
    }

    private fun cacheVeinType() {
        if (chunkVeinType == null) {
            updateData { data ->
                if (data == null) return@updateData false
                this.chunkVeinType = VeinType.REGISTERED[data.veinIdentifier]
                if (data.explored >= data.size) {
                    finished = true
                }
                return@updateData false
            }
        }
    }

    private inline fun updateData(action: (ChunkVeinData?) -> Boolean) {
        val inventory = inventoryComponent?.inventory ?: return
        val scanOutput = inventory.getStack(14).nbt ?: return
        val chunkPos = getChunkPos(scanOutput.getCompound("ChunkPos"))
        val state = ChunkVeinState.getState(world as ServerWorld)
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

    override fun getEnergyCost(): Long {
        return config.energyCost + (IRConfig.machines.drill * getActiveDrills().size)
    }

    override fun getBaseValue(enhancer: Enhancer): Double {
        val activeDrills = getActiveDrills()
        return when (enhancer) {
            Enhancer.SPEED -> activeDrills.sumOf { blockEntity ->
                blockEntity.inventory[0]
                blockEntity.getSpeedMultiplier()
            }
            Enhancer.BUFFER -> config.maxEnergyStored.toDouble()
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

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        tag?.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag?.putString("VeinIdentifier", chunkVeinType?.id.toString())
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("VeinIdentifier") == true && !tag.getString("VeinIdentifier").isNullOrEmpty())
            chunkVeinType = VeinType.REGISTERED[Identifier(tag.getString("VeinIdentifier"))]
        super.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        tag.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag.putString("VeinIdentifier", chunkVeinType?.id.toString())
        tag.put("LastMinedBlock", lastMinedItem.writeNbt(NbtCompound()))
        return tag
    }

    override fun fromClientTag(tag: NbtCompound) {
        mining = tag.getDouble("Mining")
        if (tag.contains("VeinIdentifier") && !tag.getString("VeinIdentifier").isNullOrEmpty())
            chunkVeinType = VeinType.REGISTERED[Identifier(tag.getString("VeinIdentifier"))]
        lastMinedItem = ItemStack.fromNbt(tag.getCompound("LastMinedBlock"))
    }

    companion object {
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

        const val EXPLORED_PERCENTAGE_ID = 2
        const val ENERGY_REQUIRED_ID = 3
        const val LOCATION_ID = 4
    }
}