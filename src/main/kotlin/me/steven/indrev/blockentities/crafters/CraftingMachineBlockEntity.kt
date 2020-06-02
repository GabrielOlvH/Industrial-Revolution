package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blockentities.HeatMachineBlockEntity
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeFinder
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.util.Tickable
import team.reborn.energy.EnergySide
import kotlin.math.ceil

abstract class CraftingMachineBlockEntity<T : Recipe<Inventory>>(tier: Tier, registry: MachineRegistry) :
    HeatMachineBlockEntity(tier, registry), Tickable, RecipeInputProvider, UpgradeProvider {
    var processTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[3] = this }
        }
    var totalProcessTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[4] = this }
        }

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = getInventory()
        val inputInventory = inventory.getInputInventory()
        if (inputInventory.isInvEmpty) reset()
        else if (isProcessing()) {
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
        tickTemperature(isProcessing())
        sync()
        markDirty()

    }

    abstract fun tryStartRecipe(inventory: DefaultSidedInventory): T?

    abstract fun getCurrentRecipe(): T?

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    override fun getMaxStoredPower(): Double = Upgrade.BUFFER.apply(this, getInventory())

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(5)

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun isProcessing() = processTime > 0 && energy > 0

    override fun getOptimalRange(): IntRange = 700..1100

    override fun getBaseHeatingEfficiency(): Double = 0.06

    override fun getLimitTemperature(): Double = 1400.0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 1.0 * Upgrade.SPEED.apply(this, getInventory())
        Upgrade.SPEED -> if (temperature.toInt() in this.getOptimalRange()) 2.0 else 1.0
        Upgrade.BUFFER -> baseBuffer
    }

    override fun fromTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        tag?.putInt("MaxProcessTime", totalProcessTime)
        return super.toClientTag(tag)
    }

    override fun provideRecipeInputs(recipeFinder: RecipeFinder?) {
        for (i in 0 until getInventory().invSize)
            recipeFinder?.addItem(getInventory().getInvStack(i))
    }

    open fun onCraft() {}
}