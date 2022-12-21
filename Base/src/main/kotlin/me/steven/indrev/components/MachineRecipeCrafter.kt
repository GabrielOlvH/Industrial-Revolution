package me.steven.indrev.components

import me.steven.indrev.blockentities.crafting.CraftingMachineBlockEntity
import me.steven.indrev.recipes.MachineRecipe
import me.steven.indrev.recipes.MachineRecipeProvider
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.util.Identifier

class MachineRecipeCrafter(override val syncId: Int, val provider: MachineRecipeProvider, val inputSlots: IntArray, val outputSlots: IntArray, val fluidInputSlots: IntArray, val fluidOutputSlots: IntArray) : SyncableObject {

    constructor(syncId: Int, provider: MachineRecipeProvider, inputSlots: IntArray, outputSlots: IntArray) : this(syncId, provider, inputSlots, outputSlots, intArrayOf(), intArrayOf())

    override var isDirty: Boolean = false

    var processTime = 0.0
        set(value) {
            if (field != value) isDirty = true
            field = value
        }
    var totalProcessTime = 0
        set(value) {
            if (field != value) isDirty = true
            field = value
        }
    var currentRecipe: MachineRecipe? = null

    fun tick(blockEntity: CraftingMachineBlockEntity, recipeManager: RecipeManager) {
        val inventory = blockEntity.inventory
        val fluidInventory = blockEntity.fluidInventory
        blockEntity.temperatureController.heating = false
        if (currentRecipe != null && blockEntity.useEnergy(currentRecipe!!.cost)) {
            processTime -= blockEntity.getProcessingSpeed()
            blockEntity.temperatureController.heating = true
            if (processTime <= 0)
                finishRecipe(inventory, fluidInventory, recipeManager)
            blockEntity.idle = false
        } else blockEntity.idle = true
    }

    private fun fits(inventory: MachineItemInventory, fluidInventory: MachineFluidInventory, recipe: MachineRecipe): Boolean {
        if (recipe.itemInput.size > inputSlots.size
            || recipe.itemOutput.size > outputSlots.size
            || recipe.fluidInput.size > fluidInputSlots.size
            || recipe.fluidOutput.size > fluidOutputSlots.size) return false

        inputSlots.forEachIndexed { index, slotIndex ->
            val slot = inventory[slotIndex]
            if (!recipe.itemInput[index].ingredient.test(slot.resource.toStack(slot.amount.toInt()))
                || slot.amount < recipe.itemInput[index].count) {
                return false
            }
        }

        fluidInputSlots.forEachIndexed { index, slotIndex ->
            val slot = fluidInventory[slotIndex]
            if (!slot.resource.isOf(recipe.fluidInput[index].fluid)
                || slot.amount < recipe.fluidInput[index].amount) {
                return false
            }
        }

        outputSlots.forEachIndexed { index, slotIndex ->
            if (index >= recipe.itemOutput.size) return@forEachIndexed
            val slot = inventory[slotIndex]
            if (!slot.isEmpty() && (slot.isOf(recipe.itemOutput[index].item) && slot.capacity - slot.amount < recipe.itemOutput[index].count)) {
                return false
            }
        }

        fluidOutputSlots.forEachIndexed { index, slotIndex ->
            if (index >= recipe.fluidOutput.size) return@forEachIndexed
            val slot = fluidInventory[slotIndex]
            if (!slot.isEmpty() && (slot.resource.isOf(recipe.fluidOutput[index].fluid) && slot.capacity - slot.amount < recipe.itemOutput[index].count)) {
                return false
            }
        }

        return true
    }

    private fun tryStartRecipe(inventory: MachineItemInventory, fluidInventory: MachineFluidInventory, recipeManager: RecipeManager): Boolean {
        val inputStacks = inputSlots.map {
            val slot = inventory[it]
            slot.resource.toStack(slot.amount.toInt())
        }
        val matches = provider.getMatchingRecipes(recipeManager, inputStacks, emptyList())
        val recipe = matches.firstOrNull { recipe -> fits(inventory, fluidInventory, recipe) } ?: return false
        if (this.currentRecipe != recipe) {
            this.currentRecipe = recipe
            this.processTime = recipe.ticks.toDouble()
            this.totalProcessTime = recipe.ticks
        }
        return true
    }

    fun update(inventory: MachineItemInventory, fluidInventory: MachineFluidInventory, recipeManager: RecipeManager) {
        if (!tryStartRecipe(inventory, fluidInventory, recipeManager)) {
            currentRecipe = null
            processTime = 0.0
            totalProcessTime = 0
        }
    }

    private fun finishRecipe(inventory: MachineItemInventory, fluidInventory: MachineFluidInventory, recipeManager: RecipeManager) {
        inputSlots.forEachIndexed { index, slotIndex ->
            val slot = inventory[slotIndex]
            slot.decrement(currentRecipe!!.itemInput[index].count.toLong())
        }

        fluidInputSlots.forEachIndexed { index, slotIndex ->
            val slot = fluidInventory[slotIndex]
            slot.decrement(currentRecipe!!.fluidInput[index].amount)
        }

        outputSlots.forEachIndexed { index, slotIndex ->
            if (index >= currentRecipe!!.itemOutput.size) return@forEachIndexed
            val slot = inventory[slotIndex]
            val output = currentRecipe!!.itemOutput[index]
            slot.variant = ItemVariant.of(output.item)
            slot.amount = slot.amount + output.count
        }

        fluidOutputSlots.forEachIndexed { index, slotIndex ->
            if (index >= currentRecipe!!.fluidOutput.size) return@forEachIndexed
            val slot = fluidInventory[slotIndex]
            val output = currentRecipe!!.fluidOutput[index]
            slot.variant = FluidVariant.of(output.fluid)
            slot.amount = slot.amount + output.amount
        }

        if (!fits(inventory, fluidInventory, currentRecipe!!)) {
            currentRecipe = null
            processTime = 0.0
            totalProcessTime = 0

            update(inventory, fluidInventory, recipeManager)
        } else {
            processTime = totalProcessTime.toDouble()
        }
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeDouble(processTime)
        buf.writeInt(totalProcessTime)
        buf.writeString(currentRecipe?.id?.toString() ?: "")
    }

    override fun fromPacket(buf: PacketByteBuf) {
        processTime = buf.readDouble()
        totalProcessTime = buf.readInt()
        val recipeId = buf.readString()
        currentRecipe = when {
            recipeId.isNotEmpty() -> {
                val recipeManager = MinecraftClient.getInstance().networkHandler?.recipeManager
                when (val recipe = recipeManager?.get(Identifier(recipeId))?.orElse(null)) {
                    is MachineRecipe -> recipe
                    is AbstractCookingRecipe -> MachineRecipeProvider.wrapRecipe(recipe)
                    else -> null
                }
            }
            else -> null
        }
    }

    fun writeNbt(nbt: NbtCompound) {
        nbt.putDouble("processTime", this.processTime)
        nbt.putInt("totalProcessTime", this.totalProcessTime)
    }

    fun readNbt(nbt: NbtCompound) {
        this.processTime = nbt.getDouble("processTime")
        this.totalProcessTime = nbt.getInt("totalProcessTime")
    }
}