package me.steven.indrev.blockentities.miner

import me.steven.indrev.blockentities.HeatMachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.world.WorldChunkVeinData
import me.steven.indrev.world.ChunkVeinType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import team.reborn.energy.EnergySide

class MinerBlockEntity(tier: Tier) : HeatMachineBlockEntity(tier, MachineRegistry.MINER_REGISTRY), UpgradeProvider {

    private var chunkVeinType: ChunkVeinType? = null
        set(value) {
            field = value.apply { propertyDelegate[3] = this?.ordinal ?: -1 }
        }
    private var mining = 0.0

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        if (chunkVeinType == null) {
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val state =
                (world as ServerWorld).persistentStateManager.getOrCreate(
                    { WorldChunkVeinData() },
                    WorldChunkVeinData.STATE_KEY
                )
            this.chunkVeinType = state.veins[chunkPos]?.chunkVeinType
        } else if (takeEnergy(Upgrade.ENERGY.apply(this, getInventory()))) {
            mining += Upgrade.SPEED.apply(this, getInventory())
            if (mining > 20) {
                val chunkPos = world?.getChunk(pos)?.pos ?: return
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData() },
                        WorldChunkVeinData.STATE_KEY
                    )
                val data = state.veins[chunkPos]
                if (data == null) {
                    chunkVeinType = null
                    return
                } else if (data.explored >= data.size) return
                data.explored++
                propertyDelegate[4] = data.explored * 100 / data.size
                state.markDirty()
                mining = 0.0
                getInventory().add(ItemStack(chunkVeinType!!.ores.random()))
            }
            tickTemperature(true)
        } else tickTemperature(false)
        markDirty()
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getOptimalRange(): IntRange = 200..800

    override fun getBaseHeatingEfficiency(): Double = 0.06

    override fun getLimitTemperature(): Double = 1000.0

    override fun createInventory(): DefaultSidedInventory = DefaultSidedInventory(15, intArrayOf(), (3..10).toList().toIntArray()) { slot, stack ->
        val item = stack?.item
        when {
            item is UpgradeItem -> getUpgradeSlots().contains(slot)
            item is RechargeableItem && item.canOutput -> slot == 0
            item is CoolerItem -> slot == 1
            slot in 3..10 -> true
            else -> false
        }
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(5)

    override fun getUpgradeSlots(): IntArray = intArrayOf(11, 12, 13, 14)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 128.0 + Upgrade.SPEED.apply(this, getInventory())
        Upgrade.SPEED -> if (temperature.toInt() in this.getOptimalRange()) 0.03 else 0.01
        Upgrade.BUFFER -> baseBuffer
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        tag?.putString("ChunkVeinType", chunkVeinType.toString())
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        chunkVeinType = ChunkVeinType.valueOf(tag?.getString("ChunkVeinType"))
        super.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Mining", mining)
        tag?.putString("ChunkVeinType", chunkVeinType.toString())
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        mining = tag?.getDouble("Mining") ?: 0.0
        chunkVeinType = ChunkVeinType.valueOf(tag?.getString("ChunkVeinType"))
        super.fromClientTag(tag)
    }

}