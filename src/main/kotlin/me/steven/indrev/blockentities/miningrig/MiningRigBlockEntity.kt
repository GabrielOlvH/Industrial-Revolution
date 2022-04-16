package me.steven.indrev.blockentities.miningrig

import me.steven.indrev.api.OreDataCards
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.*
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.IRConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.misc.OreDataCardItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.EnumSet

class MiningRigBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MINING_RIG_REGISTRY, pos, state) {

    init {
        this.inventoryComponent = inventory(this) {
            input {
                0 filter { itemStack, _ -> itemStack.item is OreDataCardItem }
            }
        }
        trackLong(ENERGY_REQUIRED_ID) { getEnergyCost() }
        trackInt(MAX_SPEED_ID) {
            OreDataCards.readNbt(inventoryComponent!!.inventory.getStack(0))?.speed ?: 0
        }
        VALID_DRILL_POSITIONS.forEachIndexed { index, offset ->
            val id = START_DRILL_ID + index
            val drillPos = pos.add(offset)
            trackDouble(id) {
                val block = world?.getBlockState(drillPos)?.block as? DrillBlock ?: return@trackDouble 0.0
                val drillBlockEntity = world?.getBlockEntity(block.part.getBlockEntityPos(drillPos)) as? DrillBlockEntity ?: return@trackDouble 0.0
                drillBlockEntity.miningProgress
            }
        }
    }

    override val syncToWorld: Boolean = true

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    var lastMinedItem: ItemStack = ItemStack.EMPTY

    val storageDirections: EnumSet<Direction> = EnumSet.allOf(Direction::class.java)
    private val remainingStacks = mutableListOf<ItemStack>()

    override fun machineTick() {
        val inventory = inventoryComponent?.inventory ?: return

        if (remainingStacks.isNotEmpty()) {
            val copy = ArrayList(remainingStacks)
            remainingStacks.clear()
            copy.forEach { output(it) }

            getActiveDrills().forEach { drill -> drill.setWorkingState(false) }
            workingState = false
            return
        }

        val cardStack = inventory.getStack(0)
        val data = OreDataCards.readNbt(cardStack)

        if (data != null && data.isValid() && !data.isEmpty() && use(getEnergyCost())) {
            workingState = true
            val before = data.used
            getActiveDrills().forEach { drill -> drill.tickMining(this, data) }
            if (before != data.used)
                OreDataCards.writeNbt(cardStack, data)
        } else {
            workingState = false
            getActiveDrills().forEach { drill ->
                drill.setWorkingState(false)
                drill.miningProgress = 0.0
            }
        }
    }

    fun output(stack: ItemStack) {
        val variant = ItemVariant.of(stack)
        var count = stack.count.toLong()
        storageDirections.removeIf { dir ->
            val itemStorage = itemStorageOf(world!!, pos.offset(dir), dir.opposite) ?: return@removeIf true
            if (count > 0) {
                transaction { tx ->
                    val inserted = itemStorage.insert(variant, count, tx)
                    tx.commit()
                    count -= inserted
                }
            }

            false
        }

        if (count > 0) {
            stack.count = count.toInt()
            remainingStacks.add(stack)
        }
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
        return config.energyCost + (IRConfig.machines.drill * getActiveDrills().size) + (OreDataCards.readNbt(inventoryComponent!!.inventory.getStack(0))?.energyRequired ?: 0)
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

    override fun toClientTag(tag: NbtCompound) {
        tag.put("LastMinedBlock", lastMinedItem.writeNbt(NbtCompound()))
    }

    override fun fromClientTag(tag: NbtCompound) {
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

        const val ENERGY_REQUIRED_ID = 2
        const val MAX_SPEED_ID = 3
        const val START_DRILL_ID = 4
    }
}