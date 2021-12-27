package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import com.google.common.base.Preconditions
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.components.*
import me.steven.indrev.components.multiblock.definitions.BoilerStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.gui.screenhandlers.machines.BoilerScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BoilerBlockEntity(pos: BlockPos, state: BlockState)
    : LootableContainerBlockEntity(IRBlockRegistry.BOILER_BLOCK_ENTITY, pos, state), ComponentProvider, Syncable {

    val guiSyncableComponent = GuiSyncableComponent()

    val multiblockComponent = BoilerMultiblockComponent()
    val temperatureComponent = TemperatureComponent(this,0.1, 700..1000, 1200)
    val fluidComponent = BoilerFluidComponent()

    val processTime by autosync(PROCESS_TIME_ID, 0)
    val totalProcessTime by autosync(TOTAL_PROCESS_TIME_ID, 0)

    init {
        trackObject(MOLTEN_SALT_TANK, fluidComponent[0])
        trackObject(WATER_TANK, fluidComponent[1])
        trackObject(STEAM_TANK, fluidComponent[2])
    }

    var inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
    var solidifiedSalt = 0L

    companion object {
        const val TEMPERATURE_ID = 2
        const val MAX_TEMPERATURE_ID = 3

//TODO use crafting components for this
        const val PROCESS_TIME_ID = 4
        const val TOTAL_PROCESS_TIME_ID = 5
        const val MOLTEN_SALT_TANK = 6
        const val WATER_TANK = 7
        const val STEAM_TANK = 8

        val FLUID_VALVES_MAPPER = Long2LongOpenHashMap()
        val MAX_CAPACITY: Long = bucket * 8
        val MOLTEN_SALT_AMOUNT = SolarPowerPlantSmelterBlockEntity.MOLTEN_SALT_AMOUNT

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: BoilerBlockEntity) {
            blockEntity.multiblockComponent.tick(world, pos, state)
            if (blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                blockEntity.interactWithTanks()

                val moltenSaltVolume = blockEntity.fluidComponent[0]
                val hasMoltenSalt = moltenSaltVolume.variant.isOf(IRFluidRegistry.MOLTEN_SALT_STILL)
                blockEntity.temperatureComponent.tick(hasMoltenSalt)
                blockEntity.solidifiedSalt += moltenSaltVolume.extract(scrap, true)

                if (blockEntity.solidifiedSalt >= MOLTEN_SALT_AMOUNT * 3) {
                    blockEntity.solidifiedSalt = 0
                    val stack = blockEntity.inventory[0]
                    if (stack.isEmpty) blockEntity.inventory[0] = ItemStack(IRItemRegistry.SALT, 1)
                    else if (stack.count < blockEntity.maxCountPerStack) stack.increment(1)
                }

                val waterVolume = blockEntity.fluidComponent[1]
                val steamVolume = blockEntity.fluidComponent[2]

                if (waterVolume.isEmpty || steamVolume.amount >= blockEntity.fluidComponent.getTankCapacity(2)) return

                val waterSteamConversionRate = blockEntity.temperatureComponent.temperature.toLong() - 100
                val amountToConvert = waterVolume.amount
                    .coerceAtMost(steamVolume.capacity - steamVolume.amount)
                    .coerceAtMost(waterSteamConversionRate)

                waterVolume.extract(amountToConvert, true)

                steamVolume.insert(FluidVariant.of(IRFluidRegistry.STEAM_STILL), amountToConvert, true)

            }
        }
    }

    private fun interactWithTanks() {
        //TODO
        /*
        val inputTanks = BoilerStructureDefinition.getInputTankPositions(pos, cachedState)
        inputTanks.forEach { tankPos ->
            val extractable = FluidAttributes.EXTRACTABLE.get(world, tankPos)
            FluidVolumeUtil.move(extractable, fluidComponent)
        }

        val outputTankPos = BoilerStructureDefinition.getSteamOutputTankPos(pos, cachedState)
        val insertable = FluidAttributes.INSERTABLE.get(world, outputTankPos, SearchOptions.inDirection(Direction.UP))
        FluidVolumeUtil.move(fluidComponent.getTank(2), insertable)*/

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
            ComponentKey.GUI_SYNCABLE -> guiSyncableComponent
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

    override fun writeNbt(tag: NbtCompound) {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
        Inventories.writeNbt(tag, inventory)
        super.writeNbt(tag)
    }

    fun sync() {
        Preconditions.checkNotNull(world) // Maintain distinct failure case from below
        check(world is ServerWorld) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        (world as ServerWorld).chunkManager.markForUpdate(getPos())
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

        override fun getTankCapacity(index: Int): Long {
            return if (index == 0) bucket else super.getTankCapacity(index)
        }

        override fun isFluidValidForTank(index: Int, variant: FluidVariant): Boolean {
            return when (index) {
                0 -> variant.isOf(IRFluidRegistry.MOLTEN_SALT_STILL)
                1 -> variant == FluidKeys.WATER
                2 -> variant.isOf(IRFluidRegistry.STEAM_STILL)
                else -> false
            }
        }
    }
}