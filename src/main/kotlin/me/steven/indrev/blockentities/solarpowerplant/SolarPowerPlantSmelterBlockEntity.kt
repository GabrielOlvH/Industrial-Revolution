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
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SolarPowerPlantSmelterBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_SMELTER_BLOCK_ENTITY, pos, state),
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
                val modifier = ((blockEntity.temperatureComponent.temperature - 700) / (1200.0 - 700.0)).coerceAtMost(1.0)
                stackTemperatures[slot] = Pair(meltingStack, (temp + modifier).coerceAtMost(800.0))

                val temp = stackTemperatures[slot].second

                val volume = FluidKeys.get(IRFluidRegistry.MOLTEN_SALT_STILL).withAmount(MOLTEN_SALT_AMOUNT)
                val fluidComponent = blockEntity.fluidComponent

                if (temp >= 800 && fluidComponent.attemptInsertion(volume, Simulation.SIMULATE).isEmpty) {
                    inventory[slot] = ItemStack.EMPTY
                    fluidComponent.insert(volume)
                }
            }
        }
    }

    override fun size(): Int = inventory.size

    override fun getAvailableSlots(side: Direction?): IntArray = stackTemperatures.indices.map { it }.toIntArray()

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

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        readNbt(tag, inventory, stackTemperatures)
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        writeNbt(tag, inventory, stackTemperatures)
        return super.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        readNbt(tag, inventory, stackTemperatures)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        writeNbt(tag, inventory, stackTemperatures)
        return tag
    }

    companion object {

        val MOLTEN_SALT_AMOUNT: FluidAmount = FluidAmount.of(1, 4)

        fun readNbt(tag: NbtCompound, stacks: DefaultedList<ItemStack>, temps: Array<Pair<ItemStack, Double>>) {
            val listTag = tag.getList("Items", 10)
            for (i in listTag.indices) {
                val NbtCompound = listTag.getCompound(i)
                val j: Int = NbtCompound.getByte("Slot").toInt() and 255
                if (j >= 0 && j < stacks.size) {
                    stacks[j] = ItemStack.fromNbt(NbtCompound)
                    temps[j] = Pair(stacks[j].copy(), NbtCompound.getDouble("Temp"))
                }
            }
        }

        fun writeNbt(
            tag: NbtCompound,
            stacks: DefaultedList<ItemStack>,
            temps: Array<Pair<ItemStack, Double>>
        ): NbtCompound {
            val listTag = NbtList()
            for (i in stacks.indices) {
                val itemStack = stacks[i]
                if (!itemStack.isEmpty) {
                    val NbtCompound = NbtCompound()
                    NbtCompound.putByte("Slot", i.toByte())
                    itemStack.writeNbt(NbtCompound)
                    NbtCompound.putDouble("Temp", temps[i].second)
                    listTag.add(NbtCompound)
                }
            }
            tag.put("Items", listTag)
            return tag
        }

    }
}