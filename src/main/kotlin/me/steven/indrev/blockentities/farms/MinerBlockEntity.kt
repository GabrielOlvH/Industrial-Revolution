package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.IRScanOutputItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.ChunkVeinType
import me.steven.indrev.world.chunkveins.WorldChunkVeinData
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class MinerBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.MINER_REGISTRY), UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(4)
        this.inventoryComponent = InventoryComponent {
            IRInventory(15, EMPTY_INT_ARRAY, (1 until 10).toList().toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    item is IRScanOutputItem -> slot == 14
                    slot in 1 until 10 -> true
                    else -> false
                }
            }
        }
    }

    private var chunkVeinType: ChunkVeinType? = null
    private var mining = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        if (chunkVeinType == null) {
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val state =
                (world as ServerWorld).persistentStateManager.getOrCreate(
                    { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                    WorldChunkVeinData.STATE_OVERWORLD_KEY
                )
            val data = state.veins[chunkPos] ?: return
            this.chunkVeinType = data.chunkVeinType
            propertyDelegate[3] = data.explored * 100 / data.size
            if (data.explored >= data.size) {
                mining = -1.0
                return
            }
        } else {
            val scanOutput = inventory.getStack(14).tag ?: return
            val scanChunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            if (chunkPos == scanChunkPos && mining >= 0 && Energy.of(this).use(Upgrade.ENERGY(this))) {
                mining += Upgrade.SPEED(this)
                temperatureComponent?.tick(true)
            } else {
                setWorkingState(false)
                temperatureComponent?.tick(false)
            }
            if (mining > 10) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                        WorldChunkVeinData.STATE_OVERWORLD_KEY
                    )
                val data = state.veins[chunkPos]
                if (data == null) {
                    chunkVeinType = null
                    return
                }
                val (_, size, explored) = data
                propertyDelegate[3] = explored * 100 / size
                if (explored >= size) {
                    mining = -1.0
                    return
                }
                data.explored++
                state.markDirty()
                mining = 0.0
                inventory.addStack(ItemStack(chunkVeinType!!.ores.pickRandom(world?.random)))
                setWorkingState(true)
            }
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(10, 11, 12, 13)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> getConfig().energyCost + Upgrade.SPEED(this)
        Upgrade.SPEED -> getConfig().processSpeed
        Upgrade.BUFFER -> getBaseBuffer()
    }

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag?.putString("ChunkVeinType", chunkVeinType.toString())
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("ChunkVeinType") == true && !tag.getString("ChunkVeinType").isNullOrEmpty())
            chunkVeinType = ChunkVeinType.valueOf(tag.getString("ChunkVeinType"))
        super.fromTag(state, tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        if (chunkVeinType != null)
            tag?.putString("ChunkVeinType", chunkVeinType.toString())
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        if (tag?.contains("ChunkVeinType") == true && !tag.getString("ChunkVeinType").isNullOrEmpty())
            chunkVeinType = ChunkVeinType.valueOf(tag.getString("ChunkVeinType"))
        super.fromClientTag(tag)
    }

    fun getConfig() = IndustrialRevolution.CONFIG.machines.miner
}