package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.misc.IRScanOutputItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.VeinType
import me.steven.indrev.world.chunkveins.WorldChunkVeinData
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class MinerBlockEntity(tier: Tier, private val matchScanOutput: Boolean) : MachineBlockEntity(tier, if (matchScanOutput) MachineRegistry.MINER_REGISTRY else MachineRegistry.ENDER_MINER_REGISTRY), UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(4)
        this.inventoryComponent = InventoryComponent {
            IRInventory(15, EMPTY_INT_ARRAY, (1 until 10).toList().toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    item is IRScanOutputItem -> slot == 14
                    slot in 1 until 10 -> true
                    else -> false
                }
            }
        }
    }

    private var chunkVeinType: VeinType? = null
    private var mining = 0.0
    private var finished = false

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        if (chunkVeinType == null) {
            val scanOutput = inventory.getStack(14).tag ?: return
            val chunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
            val state =
                (world as ServerWorld).persistentStateManager.getOrCreate(
                    { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                    WorldChunkVeinData.STATE_OVERWORLD_KEY
                )
            val data = state.veins[chunkPos] ?: return
            this.chunkVeinType = VeinType.REGISTERED[data.veinIdentifier]
            propertyDelegate[3] = data.explored * 100 / data.size
            if (data.explored >= data.size) {
                finished = true
                return
            }
        } else if (!finished) {
            val scanOutput = inventory.getStack(14).tag ?: return
            val scanChunkPos = getChunkPos(scanOutput.getString("ChunkPos"))
            val chunkPos = world?.getChunk(pos)?.pos ?: return
            val upgrades = getUpgrades(inventory)
            if ((chunkPos == scanChunkPos || !matchScanOutput) && Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) {
                mining += Upgrade.getSpeed(upgrades, this)
                temperatureComponent?.tick(true)
            } else {
                setWorkingState(false)
                temperatureComponent?.tick(false)
            }
            if (mining >= getConfig().processSpeed) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                        WorldChunkVeinData.STATE_OVERWORLD_KEY
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
                inventory.addStack(ItemStack(chunkVeinType!!.outputs.pickRandom(world?.random)))
                setWorkingState(true)
            }
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getUpgradeSlots(): IntArray = intArrayOf(10, 11, 12, 13)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> getConfig().energyCost
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
    }

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

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

    fun getConfig() =
        if (matchScanOutput) IndustrialRevolution.CONFIG.machines.enderMiner
        else IndustrialRevolution.CONFIG.machines.miner
}