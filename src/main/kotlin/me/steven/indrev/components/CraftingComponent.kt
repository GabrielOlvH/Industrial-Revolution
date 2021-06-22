package me.steven.indrev.components

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.api.machines.properties.Property
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.PropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import kotlin.math.ceil

open class CraftingComponent<T : IRRecipe>(index: Int, val machine: CraftingMachineBlockEntity<T>) : PropertyDelegateHolder {
    var processTime: Int by Property(4 + (index * 2), 0)
    var totalProcessTime: Int by Property(5 + (index * 2), 0)
    val fluidComponent: FluidComponent? get() = machine.fluidComponent
    val inventoryComponent: InventoryComponent get() = machine.inventoryComponent!!
    val temperatureComponent: TemperatureComponent? get() = machine.temperatureComponent
    val type: IRecipeGetter<T> get() = machine.type
    val world: World? get() = machine.world
    var isCrafting: Boolean = false

    var inputSlots: IntArray? = null
        get() = if (field == null) inventoryComponent.inventory.inputSlots else field
    var outputSlots: IntArray? = null
        get() = if (field == null) inventoryComponent.inventory.outputSlots else field

    private var currentRecipe: T? = null

    fun tick() {
        val inventory = inventoryComponent.inventory
        val inputInventory = inputSlots!!.map { inventory.getStack(it) }
        val inputTanks = fluidComponent?.inputTanks?.map { fluidComponent!![it] } ?: emptyList()
        when {
            isProcessing() -> {
                val recipe = currentRecipe
                val upgrades = machine.getEnhancers()
                if (recipe?.matches(inputInventory, inputTanks) != true)
                    tryStartRecipe(inventory) ?: reset()
                else if (machine.use(machine.getEnergyCost())) {
                    isCrafting = true
                    processTime = (processTime + ceil(Enhancer.getSpeed(upgrades, machine))).coerceAtLeast(0.0).toInt()
                    if (processTime >= totalProcessTime) {
                        handleInventories(inventory, inputInventory, recipe)
                        machine.usedRecipes.addTo(recipe.id, 1)
                        reset()
                    }
                } else isCrafting = false
            }
            machine.energy > 0 -> {
                reset()
                if (tryStartRecipe(inventory) == null) isCrafting = false
            }
            else -> {
                reset()
                isCrafting = false
            }
        }
        temperatureComponent?.tick(isProcessing() && isCrafting)
    }

    protected open fun handleInventories(inventory: IRInventory, inputInventory: List<ItemStack>, recipe: IRRecipe) {
        val output = recipe.craft(machine.world!!.random)
        inputSlots!!.forEachIndexed { index, slot ->
            recipe.input.forEach { (ingredient, count) ->
                val stack = inputInventory[index]
                if (!ingredient.test(stack)) return@forEach
                val item = stack.item
                if (item.hasRecipeRemainder())
                    inventory.setStack(slot, ItemStack(item.recipeRemainder))
                else {
                    stack.decrement(count)
                    inventory.setStack(slot, stack)
                }
                return@forEachIndexed
            }
        }

        output.forEach { stack -> craft(stack) }

        if (recipe is IRFluidRecipe) {
            fluidComponent!!.inputTanks.forEachIndexed { index, slot ->
                recipe.fluidInput.forEach { volume ->
                    val tank = fluidComponent!![slot]
                    if (tank.fluidKey != volume.fluidKey) return@forEach
                    val amount = tank.amount() - volume.amount()
                    fluidComponent!![slot] = tank.fluidKey.withAmount(amount)
                    return@forEachIndexed
                }
            }
            recipe.fluidOutput.forEach { craft(it.copy()) }

        }
    }

    fun craft(fluid: FluidVolume) {
        val fluidComponent = fluidComponent!!
        for (outputSlot in fluidComponent.outputTanks) {
            val outStack = fluidComponent[outputSlot]
            if (outStack.isEmpty || fluid.fluidKey == outStack.fluidKey && fluid.amount() + outStack.amount() <= fluidComponent.getMaxAmount_F(outputSlot))
                fluidComponent[outputSlot] = fluid.fluidKey.withAmount(outStack.amount() + fluid.amount())
            else continue
            break
        }
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

    private fun tryStartRecipe(inventory: IRInventory): T? {
        val inputStacks = inputSlots!!.map { inventory.getStack(it) }
        val inputFluids = fluidComponent?.inputTanks?.map { fluidComponent!![it] } ?: emptyList()
        val recipe =
            type.getMatchingRecipe(world as ServerWorld, inputStacks, inputFluids)
                .firstOrNull { it.matches(inputStacks, inputFluids) } ?: return null
        if (!recipe.canStart(this)) return null
        processTime = 0
        totalProcessTime = recipe.ticks
        this.currentRecipe = recipe
        return recipe
    }

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    private fun isProcessing() = totalProcessTime > 0 && processTime < totalProcessTime

    override fun getPropertyDelegate(): PropertyDelegate = machine.propertyDelegate

    fun readNbt(tag: NbtCompound?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("ProcessTime", processTime)
        tag.putInt("MaxProcessTime", totalProcessTime)
        return tag
    }
}