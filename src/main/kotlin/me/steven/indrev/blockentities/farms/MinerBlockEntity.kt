package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryController
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
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
        this.inventoryController = InventoryController {
            IRInventory(14, EMPTY_INT_ARRAY, (1 until 10).toList().toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot in 1 until 10 -> true
                    else -> false
                }
            }
        }
    }

    private var chunkVeinType: ChunkVeinType? = null
    private var mining = 0.0

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = inventoryController?.inventory ?: return
        if (chunkVeinType == null) {
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val state =
                (world as ServerWorld).persistentStateManager.getOrCreate(
                    { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                    WorldChunkVeinData.STATE_OVERWORLD_KEY
                )
            this.chunkVeinType = state.veins[chunkPos]?.chunkVeinType
        } else {
            if (mining >= 0 && Energy.of(this).use(Upgrade.ENERGY.apply(this, inventory))) {
                mining += Upgrade.SPEED.apply(this, inventory)
                temperatureController?.tick(true)
            } else {
                setWorkingState(false)
                temperatureController?.tick(false)
            }
            if (mining > 10) {
                val chunkPos = world?.getChunk(pos)?.pos ?: return
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
        update()
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(10, 11, 12, 13)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 256.0 + Upgrade.SPEED.apply(this, inventoryController!!.inventory)
        Upgrade.SPEED -> if (temperatureController?.isFullEfficiency() == true) 0.5 else 0.3
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