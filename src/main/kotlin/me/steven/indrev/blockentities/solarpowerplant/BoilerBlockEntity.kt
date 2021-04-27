package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.BoilerStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.gui.screenhandlers.machines.BoilerScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.MB
import me.steven.indrev.utils.createWrapper
import me.steven.indrev.utils.minus
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BoilerBlockEntity
    : LootableContainerBlockEntity(IRBlockRegistry.BOILER_BLOCK_ENTITY), BlockEntityClientSerializable, Tickable, PropertyDelegateHolder {

    val propertyDelegate = ArrayPropertyDelegate(4)
    val multiblockComponent = BoilerMultiblockComponent()
    val fluidComponent = BoilerFluidComponent()
    val temperatureComponent = TemperatureComponent({ null }, 0.1, 700..1000, 2000.0, { this })
    var inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
    var solidifiedSalt = 0.0

    override fun tick() {
        multiblockComponent.tick(world!!, pos, cachedState)
        if (multiblockComponent.isBuilt(world!!, pos, cachedState)) {
            val moltenSaltVolume = fluidComponent.getTank(0)
            val hasMoltenSalt = moltenSaltVolume.get().fluidKey.rawFluid!!.matchesType(IRFluidRegistry.MOLTEN_SALT_STILL)
            temperatureComponent.tick(hasMoltenSalt)
            solidifiedSalt += moltenSaltVolume.extract(MB).amount().asInexactDouble()

            if (solidifiedSalt >= MOLTEN_SALT_AMOUNT.asInexactDouble() * 2.5) {
                solidifiedSalt = 0.0
                val stack = inventory[0]
                if (stack.isEmpty) inventory[0] = ItemStack(IRItemRegistry.SALT, 1)
                else if (stack.count < maxCountPerStack) stack.increment(1)
            }

            val waterVolume = fluidComponent.getTank(1)
            val steamVolume = fluidComponent.getTank(2)
            if (waterVolume.get().isEmpty || steamVolume.get().amount() >= MAX_CAPACITY) return

            val waterSteamConversionRate = FluidAmount.ofWhole(temperatureComponent.temperature.toLong()).sub(100L).div(500L)
            if (waterSteamConversionRate.isNegative || waterSteamConversionRate.isOverflow) return
            val amountToConvert = waterVolume.get().amount()
                .coerceAtMost(steamVolume.maxAmount_F - steamVolume.get().amount())
                .coerceAtMost(waterSteamConversionRate)

            waterVolume.extract(amountToConvert)
            steamVolume.insert(FluidKeys.get(IRFluidRegistry.STEAM_STILL).withAmount(amountToConvert))

        }
    }

    override fun size(): Int = 1

    override fun getContainerName(): Text = LiteralText("Boiler")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return BoilerScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventory

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        this.inventory = list
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        temperatureComponent.fromTag(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.fromTag(tag)
        Inventories.fromTag(tag, inventory)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        temperatureComponent.toTag(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.toTag(tag)
        Inventories.toTag(tag, inventory)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        temperatureComponent.fromTag(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.fromTag(tag)
        Inventories.fromTag(tag, inventory)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        temperatureComponent.toTag(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.toTag(tag)
        Inventories.toTag(tag, inventory)
        return tag
    }

    override fun getPropertyDelegate(): PropertyDelegate = propertyDelegate

    inner class BoilerMultiblockComponent :  MultiBlockComponent({ id -> id.structure == "boiler" }, { _, _, _ -> BoilerStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            BoilerStructureDefinition.getFluidValvePositions(pos, blockState)
                .forEach { valvePos ->
                    val valveBlockState = world.getBlockState(valvePos)
                    if (valveBlockState.isOf(IRBlockRegistry.FLUID_VALVE))
                        FLUID_VALVES_MAPPER[valvePos.asLong()] = pos.asLong()
                    else
                        FLUID_VALVES_MAPPER.remove(valvePos.asLong())
                }

        }
    }

    inner class BoilerFluidComponent : FluidComponent(MAX_CAPACITY, 3) {

        override fun insertFluid(tank: Int, volume: FluidVolume, simulation: Simulation?): FluidVolume {
            return if (tank == 2) volume
            else super.insertFluid(tank, volume, simulation)
        }

        override fun extractFluid(
            tank: Int,
            filter: FluidFilter?,
            mergeWith: FluidVolume?,
            maxAmount: FluidAmount?,
            simulation: Simulation?
        ): FluidVolume {
            return if (tank != 2) FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
            else super.extractFluid(tank, filter, mergeWith, maxAmount, simulation)
        }

        override fun getInteractInventory(tank: Int): FluidTransferable {
            return createWrapper(tank, tank)
        }
    }

    companion object {
        val FLUID_VALVES_MAPPER = Long2LongOpenHashMap()
        val MAX_CAPACITY: FluidAmount = FluidAmount.ofWhole(2)
        val MOLTEN_SALT_AMOUNT: FluidAmount = SolarPowerPlantSmelterBlockEntity.MOLTEN_SALT_AMOUNT
    }
}