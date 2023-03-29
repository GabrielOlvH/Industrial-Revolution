package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.blocks.misc.BiomassComposterBlock
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.block.ComposterBlock
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class BiomassComposterBlockEntity(pos: BlockPos, state: BlockState) : BaseBlockEntity(IRBlockRegistry.BIOMASS_COMPOSTER_BLOCK_ENTITY, pos, state) {

    var ticks = 0
    var level = 0
    val fluidInv = BiomassComposterFluidInv()
    val itemInv = BiomassComposterItemInv()

    companion object {
        fun tick(state: BlockState, blockEntity: BiomassComposterBlockEntity) {
            val vol = blockEntity.fluidInv
            if (blockEntity.isInProgress()) {
                blockEntity.ticks++
            }
            if (blockEntity.isReady() && state[BiomassComposterBlock.CLOSED] && vol.amount > 0 && vol.resource.isOf(Fluids.WATER)) {
                blockEntity.fluidInv.variant = FluidVariant.of(IRFluidRegistry.METHANE_STILL)
                blockEntity.reset()
            } else if (blockEntity.isReady() && !blockEntity.hasFluids()) {
                blockEntity.itemInv.amount = 1
                blockEntity.itemInv.variant = ItemVariant.of(IRItemRegistry.BIOMASS)
                blockEntity.reset()
            }

            blockEntity.doneInsertionThisTick = false
        }
    }

    private var doneInsertionThisTick = false

    private fun hasFluids() = !fluidInv.isResourceBlank && fluidInv.amount > 0

    fun isReady() = level >= 7 && ticks >= getProgressTime()

    fun reset() {
        level = 0
        ticks = 0
        markDirty()
        if (!world!!.isClient)
            sync()
    }

    fun isInProgress(): Boolean {
        return when {
            level < 7 -> false
            cachedState[BiomassComposterBlock.CLOSED] -> fluidInv.variant.isOf(Fluids.WATER)
            else -> !hasFluids()
        } && ticks < getProgressTime()
    }

    fun getProgressTime() = if (!cachedState[BiomassComposterBlock.CLOSED]) 120 else 440

    inner class BiomassComposterFluidInv : SingleVariantStorage<FluidVariant>() {
        override fun canInsert(variant: FluidVariant): Boolean = variant.isOf(Fluids.WATER)

        override fun canExtract(variant: FluidVariant): Boolean = variant.isOf(IRFluidRegistry.METHANE_STILL)

        override fun getCapacity(variant: FluidVariant?): Long = bucket

        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()

      //  fun render(faces: List<FluidRenderFace?>?, vcp: VertexConsumerProvider?, matrices: MatrixStack?) {
          //  if (!variant.isBlank)
           //     FluidKeys.get(variant.fluid).withAmount(FluidAmount.BUCKET).render(faces, vcp, matrices)
       // }
    }

    inner class BiomassComposterItemInv : SingleVariantStorage<ItemVariant>() {

        override fun canInsert(variant: ItemVariant): Boolean = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.contains(variant.item) && level < 7 && !doneInsertionThisTick

        override fun insert(insertedVariant: ItemVariant, maxAmount: Long, transaction: TransactionContext?): Long {
            StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount)

            if ((insertedVariant == variant || variant.isBlank) && canInsert(insertedVariant)) {

                val chance = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getValue(variant.item)
                val insertedAmount = maxAmount.coerceAtMost(getCapacity(insertedVariant) - amount)
                var actuallyInserted = 0L
                for (x in 0 until insertedAmount) {
                    if (world!!.random.nextDouble() < chance) {
                        actuallyInserted++
                    }
                }
                if (insertedAmount > 0) {
                    updateSnapshots(transaction)
                    if (variant.isBlank) {
                        variant = insertedVariant
                        amount = actuallyInserted
                    } else {
                        amount += actuallyInserted
                    }
                    doneInsertionThisTick = true
                }
                return insertedAmount
            }

            return 0
        }

        override fun getCapacity(variant: ItemVariant): Long = 7 - level.toLong()

        override fun canExtract(variant: ItemVariant): Boolean = variant.isOf(IRItemRegistry.BIOMASS)

        override fun getBlankVariant(): ItemVariant = ItemVariant.blank()
    }

    override fun toTag(tag: NbtCompound) {
        tag.putInt("ticks", ticks)
        tag.putInt("level", level)
        tag.put("fluidVariant", fluidInv.variant.toNbt())
        tag.putLong("fluidAmt", fluidInv.amount)
        tag.put("itemVariant", itemInv.variant.toNbt())
        tag.putLong("itemAmt", itemInv.amount)
    }

    override fun fromTag(tag: NbtCompound) {
        ticks = tag.getInt("ticks")
        level = tag.getInt("level")
        fluidInv.variant = FluidVariant.fromNbt(tag.getCompound("fluidVariant"))
        fluidInv.amount = tag.getLong("fluidAmt")
        itemInv.variant = ItemVariant.fromNbt(tag.getCompound("itemVariant"))
        itemInv.amount = tag.getLong("itemAmt")
    }
}