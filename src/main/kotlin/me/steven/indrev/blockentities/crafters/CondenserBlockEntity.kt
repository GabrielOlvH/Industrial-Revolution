package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.SingleFluidTank
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.CondenserRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.createWrapper
import me.steven.indrev.utils.rawId
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class CondenserBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<CondenserRecipe>(tier, MachineRegistry.CONDENSER_REGISTRY, pos, state), BlockEntityClientSerializable {

    override val enhancerSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
            coolerSlot = 1
        }
        this.fluidComponent = object : FluidComponent({ this }, FluidAmount.ofWhole(8)) {
            init {
                this.inputTanks = intArrayOf(0)
            }
        }
        this.propertiesSize = 9
    }

    override val type: IRRecipeType<CondenserRecipe> = CondenserRecipe.TYPE

    override fun get(index: Int): Int {
        return when(index) {
            INPUT_TANK_ID -> fluidComponent!![0].amount().asInt(1000)
            INPUT_TANK_SIZE_ID -> fluidComponent!!.limit.asInt(1000)
            INPUT_TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            else -> super.get(index)
        }
    }

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) 4 else super.getMaxCount(enhancer)
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

    override fun fromClientTag(tag: NbtCompound) {
        fluidComponent!!.fromTag(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        fluidComponent!!.toTag(tag)
        return tag
    }


    companion object {
        const val INPUT_TANK_ID = 6
        const val INPUT_TANK_SIZE_ID = 7
        const val INPUT_TANK_FLUID_ID = 8
    }
}