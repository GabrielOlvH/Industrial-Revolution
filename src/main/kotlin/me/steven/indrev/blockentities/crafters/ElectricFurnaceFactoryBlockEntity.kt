package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.*
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.mixin.MixinAbstractCookingRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ElectricFurnaceFactoryBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<MixinAbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_FACTORY_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(15)
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 1300..1700, 2000.0)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(6, 8, 10, 12, 14) }
            output { slots =intArrayOf(7, 9, 11, 13, 15) }
        }
        this.craftingComponents = Array(5) { index ->
            CraftingComponent(index, this).apply {
                inputSlots = intArrayOf(6 + (index * 2))
                outputSlots = intArrayOf(6 + (index * 2) + 1)
            }
        }
        this.multiblockComponent = MultiblockComponent(
            { state, world ->
                val rotation = AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
                val ductState = world.getBlockState(pos.subtract(BlockPos(0, -1, 1).rotate(rotation)))
                if (!ductState.isOf(IRRegistry.DUCT)) return@MultiblockComponent 0
                val ductFacing = ductState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
                if (ductFacing == state.rotate(rotation)[HorizontalFacingMachineBlock.HORIZONTAL_FACING]) 1 else 0
            },
            MultiblockMatcher.Builder()
                .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRRegistry.FRAME.defaultState)
                .cube(BlockPos(0, 0, 1), 2, 3, 1, IRRegistry.SILO.defaultState)
                .cube(BlockPos(1, -1, 1), 1, 3, 1, INTAKE_STATE)
                .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE)
                .cube(BlockPos(-1, 0, 2), 1, 2, 1, CABINE_STATE)
                .add(BlockPos(-1, 0, 1), CONTROLLER_STATE)
                .add(BlockPos(-1, -1, 1), IRRegistry.WARNING_STROBE.defaultState)
                .build(this),
            MultiblockMatcher.Builder()
                .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRRegistry.FRAME.defaultState)
                .cube(BlockPos(-1, 0, 1), 2, 3, 1, IRRegistry.SILO.defaultState)
                .cube(BlockPos(-1, -1, 1), 1, 3, 1, INTAKE_STATE_INVERTED)
                .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE_INVERTED)
                .cube(BlockPos(1, 0, 2), 1, 2, 1, CABINE_STATE_INVERTED)
                .add(BlockPos(1, 0, 1), CONTROLLER_STATE)
                .add(BlockPos(1, -1, 1), IRRegistry.WARNING_STROBE.defaultState)
                .build(this)
        )
    }

    override val type: IRecipeGetter<MixinAbstractCookingRecipe>
        get() {
            val upgrades = getUpgrades(inventoryComponent!!.inventory)
            return when (upgrades.keys.firstOrNull { it == Upgrade.BLAST_FURNACE || it == Upgrade.SMOKER }) {
                Upgrade.BLAST_FURNACE -> RecipeType.BLASTING
                Upgrade.SMOKER -> RecipeType.SMOKING
                else -> RecipeType.SMELTING
            } as IRecipeGetter<MixinAbstractCookingRecipe>
        }

    override fun getUpgradeSlots(): IntArray = intArrayOf(2, 3, 4, 5)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.values()

    companion object {
        private val CONTROLLER_STATE = IRRegistry.CONTROLLER.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
        private val DUCT_STATE = IRRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
        private val CABINE_STATE = IRRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
        private val INTAKE_STATE = IRRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
        private val DUCT_STATE_INVERTED = IRRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val CABINE_STATE_INVERTED = IRRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val INTAKE_STATE_INVERTED = IRRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
    }
}