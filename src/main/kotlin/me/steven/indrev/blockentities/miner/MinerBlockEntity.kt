package me.steven.indrev.blockentities.miner

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.world.chunkveins.ChunkVeinType
import me.steven.indrev.world.chunkveins.WorldChunkVeinData
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import team.reborn.energy.EnergySide

class MinerBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.MINER_REGISTRY), UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryController = InventoryController({ this }) {
            DefaultSidedInventory(15, intArrayOf(), (3..10).toList().toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot in 3..10 -> true
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController({ this }, 0.06, 200..800, 1000.0)
    }

    private var chunkVeinType: ChunkVeinType? = null
        set(value) {
            field = value.apply { propertyDelegate[3] = this?.ordinal ?: -1 }
        }
    private var mining = 0.0

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = inventoryController?.getInventory() ?: return
        if (chunkVeinType == null) {
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val state =
                (world as ServerWorld).persistentStateManager.getOrCreate(
                    { WorldChunkVeinData() },
                    WorldChunkVeinData.STATE_KEY
                )
            this.chunkVeinType = state.veins[chunkPos]?.chunkVeinType
            sync()
            markDirty()
        } else if (takeEnergy(Upgrade.ENERGY.apply(this, inventory))) {
            mining += Upgrade.SPEED.apply(this, inventory)
            if (mining > 10) {
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
                inventory.addStack(ItemStack(chunkVeinType!!.ores.pickRandom(world?.random)))
                markDirty()
            }
            temperatureController?.tick(true)
        } else temperatureController?.tick(false)

    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(11, 12, 13, 14)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 128.0 + Upgrade.SPEED.apply(this, inventoryController!!.getInventory())
        Upgrade.SPEED -> if (temperatureController!!.temperature.toInt() in temperatureController!!.optimalRange) 0.03 else 0.01
        Upgrade.BUFFER -> baseBuffer
    }

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
}