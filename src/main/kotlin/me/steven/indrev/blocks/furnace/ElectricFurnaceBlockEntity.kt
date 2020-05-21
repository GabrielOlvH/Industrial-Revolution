package me.steven.indrev.blocks.furnace

import me.steven.indrev.blocks.ElectricBlockEntity
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.recipe.RecipeFinder
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class ElectricFurnaceBlockEntity : ElectricBlockEntity(MachineRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY), Tickable, InventoryProvider, RecipeInputProvider {
    private val inventory = DefaultSidedInventory(2)
    var processingItem: Item? = null
    var output: ItemStack? = null
    var cookTime: Int = 0
    var maxCookTime: Int = 0
    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inputStack = inventory.getInvStack(0)
        val outputStack = inventory.getInvStack(1).copy()
        if (isProcessing()) {
            if (inputStack.item == processingItem) {
                cookTime--
                if (!takeEnergy(1.0)) return
                if (cookTime <= 0) {
                    inventory.setInvStack(0, inputStack.apply { count-- })
                    if (outputStack.item == output?.item)
                        inventory.setInvStack(1, outputStack.apply { increment(output?.count ?: 0) })
                    else if (outputStack.isEmpty)
                        inventory.setInvStack(1, output?.copy())

                }
            } else
                reset()
        } else if (getEnergy() > 0 && !inputStack.isEmpty && cookTime <= 0) {
            reset()
            world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, BasicInventory(inputStack), world)?.ifPresent { recipe ->
                if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
                    cookTime = recipe.cookTime
                    processingItem = inputStack.item
                    output = recipe.output
                    maxCookTime = recipe.cookTime
                }
            }
        }
        propertyDelegate[2] = cookTime
        propertyDelegate[3] = maxCookTime
    }

    private fun reset() {
        cookTime = 0
        maxCookTime = 0
        processingItem = null
        output = null
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(4)

    override fun getMaxInput(): Double = 1.0

    override fun getMaxOutput(): Double = 0.0

    private fun isProcessing() = cookTime > 0 && getEnergy() > 0 && processingItem != null && output != null

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        cookTime = tag?.getInt("CookTime") ?: 0
        propertyDelegate[2] = cookTime
        maxCookTime = tag?.getInt("MaxCookTime") ?: 0
        propertyDelegate[3] = maxCookTime
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setInvStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("CookTime", cookTime)
        tag?.putInt("MaxCookTime", maxCookTime)
        val tagList = ListTag()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag?.put("Inventory", tagList)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        cookTime = tag?.getInt("CookTime") ?: 0
        propertyDelegate[2] = cookTime
        maxCookTime = tag?.getInt("MaxCookTime") ?: 0
        propertyDelegate[3] = maxCookTime
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setInvStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("CookTime", cookTime)
        tag?.putInt("MaxCookTime", maxCookTime)
        val tagList = ListTag()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag?.put("Inventory", tagList)
        return super.toClientTag(tag)
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun provideRecipeInputs(recipeFinder: RecipeFinder?) {
        for (i in 0 until inventory.invSize)
            recipeFinder?.addItem(inventory.getInvStack(i))
    }
}