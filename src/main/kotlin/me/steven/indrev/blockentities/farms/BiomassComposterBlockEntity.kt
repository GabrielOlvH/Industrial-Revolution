package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.item.ItemTransferable
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import me.steven.indrev.blocks.misc.BiomassComposterBlock
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.ComposterBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class BiomassComposterBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(IRBlockRegistry.BIOMASS_COMPOSTER_BLOCK_ENTITY, pos, state), BlockEntityClientSerializable, ItemTransferable {

    var ticks = 0
    var level = 0
    val fluidInv = BiomassComposterFluidInv()

    companion object {
        fun tick(state: BlockState, blockEntity: BiomassComposterBlockEntity) {
            val vol = blockEntity.fluidInv.getTank(0)
            if ((blockEntity.level >= 7
                        && (vol.get().isEmpty
                        || state[BiomassComposterBlock.CLOSED]
                        && vol.get().amount() >= vol.maxAmount_F))
                && blockEntity.ticks < 20)
                blockEntity.ticks++
            blockEntity.done = false
        }
    }

    private val simulated: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }
    private var done = false

    override fun attemptInsertion(stack: ItemStack, simulation: Simulation): ItemStack {
        if (done || level >= 7 || !ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.contains(stack.item)) return stack
        var inserted = 0
        val chance = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getValue(stack.item)
        if (simulation.isSimulate) {
            while (inserted < stack.count && level <= 7) {
                inserted++
                if (world!!.random.nextDouble() < chance) {
                    simulated.set(inserted)
                    break
                }
            }
        } else if (simulated.get() > 0) {
            inserted = simulated.get().coerceAtMost(stack.count)
            level++
            markDirty()
            if (!world!!.isClient)
                sync()
            simulated.set(0)
            done = true
        }
        return stack.copy().also { it.decrement(inserted) }
    }

    override fun attemptExtraction(filter: ItemFilter, maxAmount: Int, simulation: Simulation): ItemStack {
        if (!isReady() || hasFluids()) return ItemStack.EMPTY

        if (simulation.isAction)
            reset()

        return ItemStack(IRItemRegistry.BIOMASS)
    }

    private fun hasFluids() = !fluidInv.getTank(0).get().isEmpty

    fun isReady() = level >= 7 && ticks >= 20

    fun reset() {
        level = 0
        ticks = 0
        markDirty()
        if (!world!!.isClient)
            sync()
    }

    inner class BiomassComposterFluidInv : SimpleFixedFluidInv(1, FluidAmount.BUCKET) {
        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume {
            return if (fluid.fluidKey == FluidKeys.WATER) super.attemptInsertion(fluid, simulation)
            else fluid
        }

        override fun attemptExtraction(
            filter: FluidFilter,
            maxAmount: FluidAmount?,
            simulation: Simulation?
        ): FluidVolume {
            return if (ticks >= 20 && cachedState[BiomassComposterBlock.CLOSED]) {
                if (simulation == Simulation.ACTION)
                    reset()
                getTank(0).set(FluidKeys.get(IRFluidRegistry.METHANE_STILL).withAmount(FluidAmount.BUCKET))
                super.attemptExtraction(filter, maxAmount, simulation)
            } else FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("ticks", ticks)
        nbt.putInt("level", level)
        nbt.put("fluidInv", fluidInv.toTag())
        return super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        ticks = nbt.getInt("ticks")
        level = nbt.getInt("level")
        fluidInv.fromTag(nbt.getCompound("fluidInv"))
        super.readNbt(nbt)
    }

    override fun fromClientTag(nbt: NbtCompound) {
        ticks = nbt.getInt("ticks")
        level = nbt.getInt("level")
        fluidInv.fromTag(nbt.getCompound("fluidInv"))
    }

    override fun toClientTag(nbt: NbtCompound): NbtCompound {
        nbt.putInt("ticks", ticks)
        nbt.putInt("level", level)
        nbt.put("fluidInv", fluidInv.toTag())
        return nbt
    }

}