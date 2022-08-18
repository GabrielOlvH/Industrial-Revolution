package me.steven.indrev.components

import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.utils.IRFluidAmount
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import kotlin.math.ceil

open class CraftingComponent<T : IRRecipe>(private val index: Int, val machine: CraftingMachineBlockEntity<T>) : DefaultSyncableObject() {
    var processTime: Int = 0
    var totalProcessTime: Int = 0
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
                machine.enhancerComponent!!.enhancers
                if (recipe?.matches(inputInventory, inputTanks) != true) {
                    tryStartRecipe(inventory) ?: reset().also { markDirty() }
                }
                else if (machine.use(machine.getEnergyCost())) {
                    isCrafting = true
                    processTime = (processTime + ceil(machine.getProcessingSpeed())).coerceAtLeast(0.0).toInt()
                    markDirty()
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
        inputSlots!!.forEachIndexed { index, _ ->
            recipe.input.forEach { (ingredient, count) ->
                val stack = inputInventory[index]
                if (!ingredient.test(stack)) return@forEach
                stack.decrement(count)
                return@forEachIndexed
            }
        }

        output.forEach { stack -> craft(stack) }

        if (recipe is IRFluidRecipe) {
            fluidComponent!!.inputTanks.forEach outer@{ slot ->
                recipe.fluidInput.forEach { volume ->
                    val tank = fluidComponent!![slot]
                    if (tank.resource != volume.resource) return@forEach
                    val amount = tank.amount - volume.amount
                    if (amount <= 0)
                        tank.variant = FluidVariant.blank()
                    tank.amount = amount
                    tank.markDirty()
                    return@forEach
                }
            }
            recipe.fluidOutput.forEach { craft(it) }

        }
    }

    fun craft(fluid: IRFluidAmount) {
        val fluidComponent = fluidComponent!!
        for (outputSlot in fluidComponent.outputTanks) {
            val tank = fluidComponent[outputSlot]
            if (tank.isEmpty) {
                tank.variant = fluid.resource
                tank.amount = tank.amount + fluid.amount
                tank.markDirty()
            } else if (fluid.resource == tank.resource && fluid.amount() + tank.amount <= tank.capacity) {
                tank.amount = tank.amount + fluid.amount
                tank.markDirty()
            }
            else continue
            break
        }
    }

    fun craft(stack: ItemStack) {
        val inventory = inventoryComponent.inventory
        for (outputSlot in outputSlots!!) {
            val outStack = inventory.getStack(outputSlot)
            if (stack.item == outStack.item && stack.nbt == outStack.nbt && stack.count + outStack.count <= stack.maxCount)
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
            if (outStack.isEmpty || (stack.item == outStack.item && stack.nbt == outStack.nbt && stack.count + outStack.count <= stack.maxCount))
                return true
        }
        return false
    }

    private fun tryStartRecipe(inventory: IRInventory): T? {
        val inputStacks = inputSlots!!.map { inventory.getStack(it) }.filter { !it.isEmpty }
        val inputFluids = fluidComponent?.inputTanks?.map { fluidComponent!![it] }?.filter { !it.isEmpty } ?: emptyList()
        val recipe =
            type.getMatchingRecipe(world as ServerWorld, inputStacks, inputFluids)
                .firstOrNull { it.matches(inputStacks, inputFluids) } ?: return null
        if (!recipe.canStart(this)) return null
        processTime = 0
        totalProcessTime = recipe.ticks
        currentRecipe = recipe
        return recipe
    }

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    private fun isProcessing() = totalProcessTime > 0 && processTime < totalProcessTime

    fun readNbt(tag: NbtCompound?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("ProcessTime", processTime)
        tag.putInt("MaxProcessTime", totalProcessTime)
        return tag
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeInt(processTime)
        buf.writeInt(totalProcessTime)
    }

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        this.processTime = buf.readInt()
        this.totalProcessTime = buf.readInt()
    }
}