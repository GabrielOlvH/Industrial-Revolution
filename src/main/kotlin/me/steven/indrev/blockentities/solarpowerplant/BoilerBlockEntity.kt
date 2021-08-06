package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.blockentities.SyncableBlockEntity
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.BoilerStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.gui.screenhandlers.machines.BoilerScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.MB
import me.steven.indrev.utils.createWrapper
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.rawId
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class BoilerBlockEntity(pos: BlockPos, state: BlockState)
    : LootableContainerBlockEntity(IRBlockRegistry.BOILER_BLOCK_ENTITY, pos, state), PropertyDelegateHolder, ComponentProvider, Syncable, BlockEntityClientSerializable {

    val propertyDelegate = ArrayPropertyDelegate(15)
    val multiblockComponent = BoilerMultiblockComponent()
    val fluidComponent = BoilerFluidComponent()
    val temperatureComponent = TemperatureComponent(this,0.1, 700..1000, 1200)
    var inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
    var solidifiedSalt = 0.0

    companion object {
//TODO use crafting components for this
        const val PROCESS_TIME_ID = 4
        const val TOTAL_PROCESS_TIME_ID = 5

        const val MOLTEN_SALT_TANK_SIZE = 6
        const val MOLTEN_SALT_TANK_AMOUNT_ID = 7
        const val MOLTEN_SALT_TANK_FLUID_ID = 8

        const val WATER_TANK_SIZE = 9
        const val WATER_TANK_AMOUNT_ID = 10
        const val WATER_TANK_FLUID_ID = 11

        const val STEAM_TANK_SIZE = 12
        const val STEAM_TANK_AMOUNT_ID = 13
        const val STEAM_TANK_FLUID_ID = 14

        val FLUID_VALVES_MAPPER = Long2LongOpenHashMap()
        val MAX_CAPACITY: FluidAmount = FluidAmount.ofWhole(8)
        val MOLTEN_SALT_AMOUNT: FluidAmount = SolarPowerPlantSmelterBlockEntity.MOLTEN_SALT_AMOUNT

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: BoilerBlockEntity) {
            blockEntity.multiblockComponent.tick(world, pos, state)
            if (blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                blockEntity.interactWithTanks()

                val moltenSaltVolume = blockEntity.fluidComponent.getTank(0)
                val hasMoltenSalt = moltenSaltVolume.get().fluidKey.rawFluid!!.matchesType(IRFluidRegistry.MOLTEN_SALT_STILL)
                blockEntity.temperatureComponent.tick(hasMoltenSalt)
                blockEntity.solidifiedSalt += moltenSaltVolume.extract(MB.div(3)).amount().asInexactDouble()

                if (blockEntity.solidifiedSalt >= MOLTEN_SALT_AMOUNT.asInexactDouble() * 2.5) {
                    blockEntity.solidifiedSalt = 0.0
                    val stack = blockEntity.inventory[0]
                    if (stack.isEmpty) blockEntity.inventory[0] = ItemStack(IRItemRegistry.SALT, 1)
                    else if (stack.count < blockEntity.maxCountPerStack) stack.increment(1)
                }

                val waterVolume = blockEntity.fluidComponent.getTank(1)
                val steamVolume = blockEntity.fluidComponent.getTank(2)

                if (waterVolume.get().isEmpty || steamVolume.get().amount() >= blockEntity.fluidComponent.getMaxAmount_F(2)) return

                val waterSteamConversionRate = FluidAmount.ofWhole(blockEntity.temperatureComponent.temperature.toLong()).sub(100L).div(2500L)
                if (waterSteamConversionRate.isNegative || waterSteamConversionRate.isOverflow) return
                val amountToConvert = waterVolume.get().amount()
                    .coerceAtMost(steamVolume.maxAmount_F - steamVolume.get().amount())
                    .coerceAtMost(waterSteamConversionRate)

                waterVolume.extract(amountToConvert)

                val volumeToConvert = FluidKeys.get(IRFluidRegistry.STEAM_STILL).withAmount(amountToConvert)
                steamVolume.insert(volumeToConvert)

            }
        }
    }

    private fun interactWithTanks() {
        val inputTanks = BoilerStructureDefinition.getInputTankPositions(pos, cachedState)
        inputTanks.forEach { tankPos ->
            val extractable = FluidAttributes.EXTRACTABLE.get(world, tankPos)
            FluidVolumeUtil.move(extractable, fluidComponent)
        }

        val outputTankPos = BoilerStructureDefinition.getSteamOutputTankPos(pos, cachedState)
        val insertable = FluidAttributes.INSERTABLE.get(world, outputTankPos, SearchOptions.inDirection(Direction.UP))
        FluidVolumeUtil.move(fluidComponent.getTank(2), insertable)

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

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            ComponentKey.TEMPERATURE -> temperatureComponent
            ComponentKey.MULTIBLOCK -> multiblockComponent
            ComponentKey.PROPERTY_HOLDER -> this
            else -> null
        }
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        temperatureComponent.readNbt(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.readNbt(tag)
        Inventories.readNbt(tag, inventory)
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
        Inventories.writeNbt(tag, inventory)
        return super.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        multiblockComponent.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        multiblockComponent.writeNbt(tag)
        return tag
    }

    override fun getPropertyDelegate(): PropertyDelegate = if (world!!.isClient) propertyDelegate else object : PropertyDelegate {
        override fun get(index: Int): Int {
            return when (index) {
                MOLTEN_SALT_TANK_SIZE -> fluidComponent.getMaxAmount_F(0).asInt(1000)
                MOLTEN_SALT_TANK_AMOUNT_ID -> fluidComponent[0].amount().asInt(1000)
                MOLTEN_SALT_TANK_FLUID_ID -> fluidComponent[0].rawFluid.rawId
                WATER_TANK_SIZE -> fluidComponent.getMaxAmount_F(1).asInt(1000)
                WATER_TANK_AMOUNT_ID -> fluidComponent[1].amount().asInt(1000)
                WATER_TANK_FLUID_ID -> fluidComponent[1].rawFluid.rawId
                STEAM_TANK_SIZE -> fluidComponent.getMaxAmount_F(2).asInt(1000)
                STEAM_TANK_AMOUNT_ID -> fluidComponent[2].amount().asInt(1000)
                STEAM_TANK_FLUID_ID -> fluidComponent[2].rawFluid.rawId
                else -> -1
            }
        }

        override fun set(index: Int, value: Int) = error("Unsupported")

        override fun size(): Int = 15
    }

    override fun markForUpdate(condition: () -> Boolean) {
        //TODO
    }

    inner class BoilerMultiblockComponent : MultiBlockComponent({ id -> id.structure == "boiler" }, { _, _, _ -> BoilerStructureDefinition }) {
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

    inner class BoilerFluidComponent : FluidComponent({this}, MAX_CAPACITY, 3) {

        override fun getMaxAmount_F(tank: Int): FluidAmount {
            return if (tank == 0) FluidAmount.BUCKET else super.getMaxAmount_F(tank)
        }

        override fun isFluidValidForTank(tank: Int, fluid: FluidKey): Boolean {
            return when (tank) {
                0 -> fluid.rawFluid?.matchesType(IRFluidRegistry.MOLTEN_SALT_STILL) == true
                1 -> fluid == FluidKeys.WATER
                2 -> fluid.rawFluid?.matchesType(IRFluidRegistry.STEAM_STILL) == true
                else -> false
            }
        }

        override fun getInteractInventory(tank: Int): FluidTransferable {
            return createWrapper(tank, tank)
        }
    }
}