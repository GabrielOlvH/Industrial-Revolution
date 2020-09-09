package me.steven.indrev.blockentities.crafters

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.Property
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.ExperienceRewardRecipe
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import kotlin.math.ceil
import kotlin.math.floor

abstract class CraftingMachineBlockEntity<T : IRRecipe>(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity<BasicMachineConfig>(tier, registry), Tickable, UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    private var currentRecipe: T? = null
    protected var processTime: Int by Property(3, 0)
    private var totalProcessTime: Int by Property(4, 0)
    private val usedRecipes = mutableMapOf<Identifier, Int>()
    abstract val type: RecipeType<T>

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val inputInventory = inventory.getInputInventory()
        if (isProcessing()) {
            val recipe = currentRecipe
            val upgrades = getUpgrades(inventory)
            if (recipe?.matches(inputInventory, fluidComponent?.tanks?.get(0)?.volume) != true)
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) {
                setWorkingState(true)
                processTime = (processTime - ceil(Upgrade.getSpeed(upgrades, this))).coerceAtLeast(0.0).toInt()
                if (processTime <= 0) {
                    handleInventories(inventory, inputInventory, recipe)
                    usedRecipes[recipe.id] = usedRecipes.computeIfAbsent(recipe.id) { 0 } + 1
                    onCraft()
                    reset()
                }
            }
        } else if (energy > 0 && processTime <= 0) {
            reset()
            if (tryStartRecipe(inventory) == null) setWorkingState(false)
        }
        temperatureComponent?.tick(isProcessing())
    }

    fun handleInventories(inventory: IRInventory, inputInventory: Inventory, recipe: IRRecipe) {
        val output = recipe.craft(world!!.random)
        recipe.input.forEach { (ingredient, count) ->
            inventory.inputSlots.forEachIndexed { index, slot ->
                val stack = inputInventory.getStack(index)
                if (!ingredient.test(stack)) return@forEachIndexed
                val item = stack.item
                if (item.hasRecipeRemainder())
                    inventory.setStack(slot, ItemStack(item.recipeRemainder))
                else {
                    stack.decrement(count)
                    inventory.setStack(slot, stack)
                }
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

        output.forEachIndexed { index, stack ->
            val outputSlot = inventory.outputSlots[index]
            val outputStack = inventory.getStack(outputSlot)
            if (outputStack.item == stack.item)
                inventory.setStack(outputSlot, outputStack.apply { increment(stack.count) })
            else if (outputStack.isEmpty)
                inventory.setStack(outputSlot, stack)
        }
    }

    private fun tryStartRecipe(inventory: IRInventory): T? {
        val inputStacks = inventory.getInputInventory()
        val inputFluid = fluidComponent?.tanks?.get(0)?.volume
        val recipe = world?.recipeManager?.listAllOfType(type)
            ?.firstOrNull { it.matches(inputStacks, inputFluid) }
            ?: return null
        if (recipe is IRFluidRecipe && recipe.fluidOutput != null) {
            val tanks = if (recipe.fluidInput != null) 2 else 1
            if (fluidComponent!!.tankCount < tanks) {
                IndustrialRevolution.LOGGER.error("Attempted to start recipe ${recipe.id} which has a fluid output but machine $this is missing tank! Report this issue")
                return null
            }
            val outputTankVolume = fluidComponent!!.tanks.last().volume
            val recipeFluidOutput = recipe.fluidOutput!!
            if (!(outputTankVolume.isEmpty || (outputTankVolume.fluidKey == recipeFluidOutput.fluidKey || outputTankVolume.amount().add(recipeFluidOutput.amount()) <= fluidComponent!!.limit)))
                return null
        }
        if (inventory.outputSlots.isNotEmpty()) {
            for ((index, entry) in recipe.outputs.withIndex()) {
                val stack = entry.stack
                val outputStack = inventory.getStack(inventory.outputSlots[index])
                if (!outputStack.isEmpty && (outputStack.item != stack.item || outputStack.count + stack.count > outputStack.maxCount))
                    return null
            }
        }
        if (!isProcessing()) {
            processTime = recipe.ticks
            totalProcessTime = recipe.ticks
        }
        this.currentRecipe = recipe
        return recipe
    }

    protected fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun isProcessing() = processTime > 0 && energy > 0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> config.energyCost
        Upgrade.SPEED ->
            if (temperatureComponent?.isFullEfficiency() == true)
                (config as? HeatMachineConfig?)?.processTemperatureBoost ?: config.processSpeed
            else
                config.processSpeed
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        totalProcessTime = tag?.getInt("MaxProcessTime") ?: 0
        super.fromTag(state, tag)
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

    fun dropExperience(player: PlayerEntity) {
        val list = mutableListOf<T>()
        usedRecipes.forEach { (id, amount) ->
            world!!.recipeManager[id].ifPresent { recipe ->
                list.add(recipe as? T ?: return@ifPresent)
                spawnOrbs(
                    world!!,
                    player.pos,
                    amount,
                    ((recipe as? ExperienceRewardRecipe)?.amount ?: (recipe as? SmeltingRecipe)?.experience
                    ?: return@ifPresent)
                )
            }
        }
        player.unlockRecipes(list.toList())
        usedRecipes.clear()
    }

    private fun spawnOrbs(world: World, pos: Vec3d, amount: Int, experience: Float) {
        val xp = amount.toFloat() * experience
        var n = floor(xp).toInt()
        val decimal = xp % 1
        if (decimal != 0.0f && Math.random() < decimal.toDouble()) {
            ++n
        }
        while (n > 0) {
            val size = ExperienceOrbEntity.roundToOrbSize(n)
            n -= size
            world.spawnEntity(ExperienceOrbEntity(world, pos.x, pos.y, pos.z, size))
        }
    }

    open fun onCraft() {}
}