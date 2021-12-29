package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import com.google.common.base.Preconditions
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import me.steven.indrev.components.GuiSyncableComponent
import me.steven.indrev.components.trackDouble
import me.steven.indrev.gui.screenhandlers.machines.SolarPowerPlantSmelterScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SolarPowerPlantSmelterBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_SMELTER_BLOCK_ENTITY, pos, state), SidedInventory, ComponentProvider {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(4, ItemStack.EMPTY)

    //i know its bad i'll fix it someday sorry
    var stackTemperatures = Array<Pair<ItemStack, Double>>(4) { Pair(ItemStack.EMPTY, 12.0) }

    val guiSyncableComponent = GuiSyncableComponent()

    init {
        trackDouble(FIRST_SLOT_ID) { stackTemperatures[0].second }
        trackDouble(SECOND_SLOT_ID) { stackTemperatures[1].second }
        trackDouble(THIRD_SLOT_ID) { stackTemperatures[2].second }
        trackDouble(FOURTH_SLOT_ID) { stackTemperatures[3].second }
    }


    fun tickStacks(blockEntity: SolarPowerPlantTowerBlockEntity) {
        stackTemperatures.forEachIndexed { slot, (meltingStack, temp) ->
            val stack = inventory[slot]
            if (!ItemStack.areEqual(meltingStack, stack)) {
                stackTemperatures[slot] = Pair(stack.copy(), 12.0)
            } else if (!meltingStack.isEmpty) {
                val modifier = ((blockEntity.temperatureComponent.temperature - 700) / (1200.0 - 700.0)).coerceIn(0.0, 2.0)
                stackTemperatures[slot] = Pair(meltingStack, (temp + modifier).coerceAtMost(800.0))

                val temp = stackTemperatures[slot].second
                val fluidComponent = blockEntity.fluidComponent

                if (temp >= 800 && fluidComponent[0].tryInsert(FluidVariant.of(IRFluidRegistry.MOLTEN_SALT_STILL), MOLTEN_SALT_AMOUNT)) {
                    inventory[slot] = ItemStack.EMPTY
                    fluidComponent[0].insert(FluidVariant.of(IRFluidRegistry.MOLTEN_SALT_STILL), MOLTEN_SALT_AMOUNT, true)
                }

                markDirty()
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

    override fun writeNbt(tag: NbtCompound) {
        writeNbt(tag, inventory, stackTemperatures)
    }

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.WORLD_OBJECT -> world
            ComponentKey.GUI_SYNCABLE -> guiSyncableComponent
            else -> null
        }
    }

    companion object {

        val MOLTEN_SALT_AMOUNT = bucket / 9

        const val FIRST_SLOT_ID = 0
        const val SECOND_SLOT_ID = 1
        const val THIRD_SLOT_ID = 2
        const val FOURTH_SLOT_ID = 3

        fun readNbt(tag: NbtCompound, stacks: DefaultedList<ItemStack>, temps: Array<Pair<ItemStack, Double>>) {
            val listTag = tag.getList("Items", 10)
            for (i in listTag.indices) {
                val nbt = listTag.getCompound(i)
                val j: Int = nbt.getByte("Slot").toInt() and 255
                if (j >= 0 && j < stacks.size) {
                    stacks[j] = ItemStack.fromNbt(nbt)
                    temps[j] = Pair(stacks[j].copy(), nbt.getDouble("Temp"))
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
                val nbt = NbtCompound()
                nbt.putByte("Slot", i.toByte())
                itemStack.writeNbt(nbt)
                nbt.putDouble("Temp", temps[i].second)
                listTag.add(nbt)
            }
            tag.put("Items", listTag)
            return tag
        }

    }
}