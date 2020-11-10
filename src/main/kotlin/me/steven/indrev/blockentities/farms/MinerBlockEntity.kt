package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.misc.IRResourceReportItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinState
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
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
            coolerSlot = 1
        }
    }

    private var chunkVeinType: VeinType? = null
    private var mining = 0.0
    private var finished = false

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cacheVeinType()
        if (!finished) {
            val scanOutput = inventory.getStack(14).tag ?: return
            val scanChunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val upgrades = getUpgrades(inventory)
            if ((chunkPos == scanChunkPos) && Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) {
                val multiplier = getActiveDrills().sumByDouble { blockEntity ->
                    val itemStack = blockEntity.inventory[0]
                    if (!itemStack.isEmpty) {
                        val speed = DrillBlockEntity.getSpeedMultiplier(itemStack.item)
                        if (speed > 0) {
                            itemStack.damage++
                            if (itemStack.damage >= itemStack.maxDamage) itemStack.decrement(1)
                        }
                        speed
                    }
                    else 0.0
                }
                mining += Upgrade.getSpeed(upgrades, this) * multiplier
                temperatureComponent?.tick(true)
            } else {
                setWorkingState(false)
                temperatureComponent?.tick(false)
            }
            if (mining >= config.processSpeed) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { ChunkVeinState(ChunkVeinState.STATE_OVERWORLD_KEY) },
                        ChunkVeinState.STATE_OVERWORLD_KEY
                    )
                val data = state.veins[scanChunkPos]
                if (data == null) {
                    chunkVeinType = null
                    return
                }
                val (_, size, explored) = data
                propertyDelegate[3] = explored * 100 / size
                if (explored >= size) {
                    finished = true
                    return
                }
                data.explored++
                state.markDirty()
                mining = 0.0
                inventory.output(ItemStack(chunkVeinType!!.outputs.pickRandom(world?.random)))
                setWorkingState(true)
            }
        } else setWorkingState(false)
    }

    private fun cacheVeinType() {
        if (chunkVeinType == null) {
            val data = getVeinData() ?: return
            this.chunkVeinType = VeinType.REGISTERED[data.veinIdentifier]
            propertyDelegate[3] = data.explored * 100 / data.size
            if (data.explored >= data.size) {
                finished = true
                return
            }
        }
    }

    private fun getVeinData(): ChunkVeinData? {
        val inventory = inventoryComponent?.inventory ?: return null
        val scanOutput = inventory.getStack(14).tag ?: return null
        val chunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
        val state =
            (world as ServerWorld).persistentStateManager.getOrCreate(
                { ChunkVeinState(ChunkVeinState.STATE_OVERWORLD_KEY) },
                ChunkVeinState.STATE_OVERWORLD_KEY
            )
        return state.veins[chunkPos]
    }

    fun getActiveDrills(): List<DrillBlockEntity> {
        val offsets = arrayOf(BlockPos(-1, 0, -1), BlockPos(-1, 0, 1), BlockPos(1, 0 ,1), BlockPos(1, 0, -1))
        return offsets.map { pos.add(it) }.mapNotNull { pos ->
            val blockState = world?.getBlockState(pos)
            val block = blockState?.block
            if (block is DrillBlock) {
                val blockEntity = world?.getBlockEntity(block.part.getBlockEntityPos(pos)) as? DrillBlockEntity ?: return@mapNotNull null
                val itemStack = blockEntity.inventory[0]
                if (!itemStack.isEmpty && DrillBlockEntity.isValidDrill(itemStack.item))
                    blockEntity
                else null
            } else null
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(10, 11, 12, 13)

    override fun getAvailableUpgrades(): Array<Upgrade> = arrayOf(Upgrade.BUFFER, Upgrade.ENERGY)

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> config.energyCost
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
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
}