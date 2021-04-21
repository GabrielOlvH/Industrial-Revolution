package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.gui.screenhandlers.machines.SolarPowerPlantSmelterScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction

class SolarPowerPlantSmelterBlockEntity : LootableContainerBlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_SMELTER_BLOCK_ENTITY),
    BlockEntityClientSerializable, SidedInventory {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(12, ItemStack.EMPTY)

    //i know its bad i'll fix it someday sorry
    var stackTemperatures = Array<Pair<ItemStack, Double>>(12) { Pair(ItemStack.EMPTY, 12.0) }

    fun tickStacks(blockEntity: SolarPowerPlantTowerBlockEntity) {
        stackTemperatures.forEachIndexed { slot, (meltingStack, temp) ->
            val stack = inventory[slot]
            if (!ItemStack.areEqual(meltingStack, stack)) {
                stackTemperatures[slot] = Pair(stack.copy(), 12.0)
            } else if (!meltingStack.isEmpty) {
                val modifier = (blockEntity.temperatureComponent.temperature / 600.0).coerceAtMost(1.0)
                stackTemperatures[slot] = Pair(meltingStack, (temp + modifier * 30).coerceAtMost(800.0))

                val temp = stackTemperatures[slot].second

                val volume = FluidKeys.get(IRFluidRegistry.MOLTEN_SALT_STILL).withAmount(FluidAmount.BUCKET)
                val fluidComponent = blockEntity.fluidComponent

                if (temp >= 800 && fluidComponent.attemptInsertion(volume, Simulation.SIMULATE).isEmpty) {
                    inventory[slot] = ItemStack.EMPTY
                    fluidComponent.insert(volume)
                }
            }
        }
    }

    override fun size(): Int = inventory.size

    override fun getAvailableSlots(side: Direction?): IntArray = intArrayOf(0)

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = stack?.item == IRItemRegistry.SALT

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = false

    override fun getMaxCountPerStack(): Int = 1

    override fun getContainerName(): Text = LiteralText("Solar Power Plant Tower")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return SolarPowerPlantSmelterScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventory

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        this.inventory = list
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        fromTag(tag, inventory, stackTemperatures)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        toTag(tag, inventory, stackTemperatures)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        fromTag(tag, inventory, stackTemperatures)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        toTag(tag, inventory, stackTemperatures)
        return tag
    }

    companion object {
        fun fromTag(tag: CompoundTag, stacks: DefaultedList<ItemStack>, temps: Array<Pair<ItemStack, Double>>) {
            val listTag = tag.getList("Items", 10)
            for (i in listTag.indices) {
                val compoundTag = listTag.getCompound(i)
                val j: Int = compoundTag.getByte("Slot").toInt() and 255
                if (j >= 0 && j < stacks.size) {
                    stacks[j] = ItemStack.fromTag(compoundTag)
                    temps[j] = Pair(stacks[j].copy(), compoundTag.getDouble("Temp"))
                }
            }
        }

        fun toTag(
            tag: CompoundTag,
            stacks: DefaultedList<ItemStack>,
            temps: Array<Pair<ItemStack, Double>>
        ): CompoundTag {
            val listTag = ListTag()
            for (i in stacks.indices) {
                val itemStack = stacks[i]
                if (!itemStack.isEmpty) {
                    val compoundTag = CompoundTag()
                    compoundTag.putByte("Slot", i.toByte())
                    itemStack.toTag(compoundTag)
                    compoundTag.putDouble("Temp", temps[i].second)
                    listTag.add(compoundTag)
                }
            }
            tag.put("Items", listTag)
            return tag
        }

    }
}