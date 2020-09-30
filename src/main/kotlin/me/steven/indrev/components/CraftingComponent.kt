package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.utils.Property
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import team.reborn.energy.Energy
import kotlin.math.ceil

class CraftingComponent<T : IRRecipe>(index: Int, val machine: CraftingMachineBlockEntity<T>) : PropertyDelegateHolder {
    var processTime: Int by Property(3 + (index * 2), 0)
    var totalProcessTime: Int by Property(4 + (index * 2), 0)
    val fluidComponent: FluidComponent? get() = machine.fluidComponent
    val inventoryComponent: InventoryComponent get() = machine.inventoryComponent!!
    val temperatureComponent: TemperatureComponent? get() = machine.temperatureComponent
    val type: IRecipeGetter<T> get() = machine.type
    val world: World? get() = machine.world

    var inputSlots: IntArray? = null
        get() = if (field == null) inventoryComponent.inventory.inputSlots else field
    var outputSlots: IntArray? = null
        get() = if (field == null) inventoryComponent.inventory.outputSlots else field

    private var currentRecipe: T? = null

    fun tick() {
        val inventory = inventoryComponent.inventory
        val inputInventory = inputSlots!!.map { inventory.getStack(it) }.toTypedArray()
        if (isProcessing()) {
            val recipe = currentRecipe
            val upgrades = machine.getUpgrades(inventory)
            if (recipe?.matches(inputInventory, fluidComponent?.tanks?.get(0)?.volume) != true)
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(machine).use(Upgrade.getEnergyCost(upgrades, machine))) {
                machine.setWorkingState(true)
                processTime = (processTime + ceil(Upgrade.getSpeed(upgrades, machine))).coerceAtLeast(0.0).toInt()
                if (processTime >= totalProcessTime) {
                    handleInventories(inventory, inputInventory, recipe)
                    machine.usedRecipes[recipe.id] = machine.usedRecipes.computeIfAbsent(recipe.id) { 0 } + 1
                    machine.onCraft()
                    reset()
                }
            } else machine.setWorkingState(false)
        } else if (machine.energy > 0 && processTime <= 0) {
            reset()
            if (tryStartRecipe(inventory) == null) machine.setWorkingState(false)
        }
        temperatureComponent?.tick(isProcessing())
    }

    private fun handleInventories(inventory: IRInventory, inputInventory: Array<ItemStack>, recipe: IRRecipe) {
        val output = recipe.craft(machine.world!!.random)
        recipe.input.forEach { (ingredient, count) ->
            inputSlots!!.forEachIndexed { index, slot ->
                val stack = inputInventory[index]
                if (!ingredient.test(stack)) return@forEachIndexed
                val item = stack.item
                if (item.hasRecipeRemainder())
                    inventory.setStack(slot, ItemStack(item.recipeRemainder))
                else {
                    stack.decrement(count)
                    inventory.setStack(slot, stack)
                }
                return@forEach
            }
        }

        if (recipe is IRFluidRecipe) {
            val fluidInput = recipe.fluidInput
            if (fluidInput != null) {
                val inputTank = fluidComponent!!.tanks.first()
                val amount = inputTank.volume.amount().sub(fluidInput.amount())
                inputTank.volume = inputTank.volume.fluidKey.withAmount(amount)
            }
            val fluidOutput = recipe.fluidOutput
            if (fluidOutput != null) {
                val outputTank = fluidComponent!!.tanks.last()
                val amount = outputTank.volume.amount().add(fluidOutput.amount())
                outputTank.volume = fluidOutput.fluidKey.withAmount(amount)
            }
        }

        output.forEach { stack -> craft(stack) }
    }

    fun craft(stack: ItemStack) {
        val inventory = inventoryComponent.inventory
        for (outputSlot in outputSlots!!) {
            val outStack = inventory.getStack(outputSlot)
            if (stack.item == outStack.item && stack.tag == outStack.tag && stack.count + outStack.count <= stack.maxCount)
                outStack.increment(stack.count)
            else if (outStack.isEmpty)
                inventory.setStack(outputSlot, stack)
            else continue
            break
        }
    }

    fun fits(stack: ItemStack): Boolean {
        for (outputSlot in outputSlots!!) {
            val outStack = inventoryComponent.inventory.getStack(outputSlot)
            if (outStack.isEmpty || (stack.item == outStack.item && stack.tag == outStack.tag && stack.count + outStack.count <= stack.maxCount))
                return true
        }
        return false
    }

    fun fits(stack: ItemStack, outputSlot: Int): Boolean {
        val outStack = inventoryComponent.inventory.getStack(outputSlot)
        if (outStack.isEmpty || (stack.item == outStack.item && stack.tag == outStack.tag))
            return true
        return false
    }

    private fun tryStartRecipe(inventory: IRInventory): T? {
        val inputStacks = inputSlots!!.map { inventory.getStack(it) }.toTypedArray()
        val inputFluid = fluidComponent?.tanks?.get(0)?.volume
        val recipe = inputStacks.flatMap { type.getMatchingRecipe(world as ServerWorld, it) }.firstOrNull { it.matches(inputStacks, inputFluid) } ?: return null
        if (recipe is IRFluidRecipe && recipe.fluidOutput != null) {
            val tanks = if (recipe.fluidInput != null) 2 else 1
            if (fluidComponent!!.tankCount < tanks) {
                IndustrialRevolution.LOGGER.error("Attempted to start recipe ${recipe.id} which has a fluid output but machine $this is missing tank! Report this issue")
                return null
            }
            val outputTankVolume = fluidComponent!!.tanks.last().volume
            val recipeFluidOutput = recipe.fluidOutput!!
            if (!outputTankVolume.isEmpty && (outputTankVolume.fluidKey != recipeFluidOutput.fluidKey || outputTankVolume.amount().add(recipeFluidOutput.amount()) > fluidComponent!!.limit))
                return null
        }
        if (outputSlots!!.isNotEmpty() && recipe.outputs.any { !fits(it.stack) })
            return null
        if (!isProcessing()) {
            processTime = 0
            totalProcessTime = recipe.ticks
        }
        this.currentRecipe = recipe
        return recipe
    }

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    fun isProcessing() = totalProcessTime > 0 && processTime < totalProcessTime && machine.energy > 0

    override fun getPropertyDelegate(): PropertyDelegate = machine.propertyDelegate

    fun fromTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putInt("ProcessTime", processTime)
        tag.putInt("MaxProcessTime", totalProcessTime)
        return tag
    }
}