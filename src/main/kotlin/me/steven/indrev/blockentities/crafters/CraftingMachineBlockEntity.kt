package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blockentities.InterfacedMachineBlockEntity
import me.steven.indrev.blockentities.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeFinder
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import team.reborn.energy.EnergySide
import kotlin.math.ceil

abstract class CraftingMachineBlockEntity<T : Recipe<Inventory>>(
        type: BlockEntityType<*>,
        tier: Tier,
        baseBuffer: Double
) :
        InterfacedMachineBlockEntity(type, tier, baseBuffer), Tickable, RecipeInputProvider, UpgradeProvider, TemperatureController {
    var temperature = 300.0
        set(value) {
            field = value.coerceAtLeast(0.0).apply { propertyDelegate[2] = this.toInt() }
        }
        get() = field.apply { propertyDelegate[2] = this.toInt() }
    var cooling = 0
    var inventory: DefaultSidedInventory? = null
        get() = field ?: createInventory().apply { field = this }
    var processTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[3] = this }
        }
        get() = field.apply { propertyDelegate[3] = this }
    var totalProcessTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[4] = this }
        }
        get() = field.apply { propertyDelegate[4] = this }

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        inventory?.also { inventory ->
            val inputInventory = inventory.getInputInventory()
            if (inputInventory.isInvEmpty)
                reset()
            if (isProcessing()) {
                val recipe = getCurrentRecipe()
                if (recipe?.matches(inputInventory, this.world) == false)
                    tryStartRecipe(inventory) ?: reset()
                else if (takeEnergy(Upgrade.ENERGY.apply(this, inventory))) {
                    processTime = (processTime - ceil(Upgrade.SPEED.apply(this, inventory))).coerceAtLeast(0.0).toInt()
                    if (processTime <= 0) {
                        inventory.inputSlots.forEachIndexed { index, slot ->
                            inventory.setInvStack(slot, inputInventory.getInvStack(index).apply { decrement(1) })
                        }
                        val output = recipe?.output ?: return
                        for (outputSlot in inventory.outputSlots) {
                            val outputStack = inventory.getInvStack(outputSlot)
                            if (outputStack.item == output.item)
                                inventory.setInvStack(outputSlot, outputStack.apply { increment(output.count) })
                            else if (outputStack.isEmpty)
                                inventory.setInvStack(outputSlot, output.copy())
                            else continue
                            break
                        }
                        onCraft()
                        reset()
                    }
                } else reset()
            } else if (energy > 0 && !inputInventory.isInvEmpty && processTime <= 0) {
                reset()
                tryStartRecipe(inventory)
            }
            tickTemperature()
            markDirty()
        }
    }

    private fun tickTemperature() {
        val coolerStack = this.inventory!!.getInvStack(1)
        val coolerItem = coolerStack.item

        if (isProcessing()) {
            temperature +=
                    if (coolerItem is CoolerItem
                            && coolerStack.damage < coolerStack.maxDamage
                            && (cooling > 0
                                    || temperature + 50 >= getOptimalRange().last)) {
                        cooling--
                        if (temperature + 150 < getOptimalRange().last) cooling = 0
                        else if (cooling <= 0) {
                            cooling = 200
                            coolerStack.damage++
                        }
                        getBaseHeatingEfficiency() * coolerItem.coolingModifier
                    } else getBaseHeatingEfficiency()
        } else if (temperature > 310) temperature -= getBaseHeatingEfficiency() / 2
    }

    abstract fun tryStartRecipe(inventory: DefaultSidedInventory): T?

    abstract fun getCurrentRecipe(): T?

    abstract fun createInventory(): DefaultSidedInventory

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    override fun getMaxStoredPower(): Double = Upgrade.BUFFER.apply(this, inventory!!)

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(5)

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun isProcessing() = processTime > 0 && energy > 0

    override fun getCurrentTemperature(): Double = temperature

    override fun setCurrentTemperature(temperature: Double) {
        this.temperature = temperature
    }

    override fun getOptimalRange(): IntRange = 700..1100

    override fun getBaseHeatingEfficiency(): Double = 0.06

    override fun getLimitTemperature(): Double = 1400.0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 1.0 * Upgrade.SPEED.apply(this, inventory!!)
        Upgrade.SPEED -> if (temperature.toInt() in this.getOptimalRange()) 2.0 else 1.0
        Upgrade.BUFFER -> baseBuffer
    }

    override fun fromTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        temperature = tag?.getDouble("Temperature") ?: 0.0
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        tag?.putDouble("Temperature", temperature)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        temperature = tag?.getDouble("Temperature") ?: 0.0
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        tag?.putDouble("Temperature", temperature)
        return super.toClientTag(tag)
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory!!

    override fun provideRecipeInputs(recipeFinder: RecipeFinder?) {
        for (i in 0 until inventory!!.invSize)
            recipeFinder?.addItem(inventory!!.getInvStack(i))
    }

    open fun onCraft() {}
}