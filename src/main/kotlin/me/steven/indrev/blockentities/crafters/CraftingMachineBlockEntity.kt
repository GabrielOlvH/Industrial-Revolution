package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.Property
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.ExperienceRewardRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Recipe
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

abstract class CraftingMachineBlockEntity<T : Recipe<Inventory>>(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity(tier, registry), Tickable, UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    protected var processTime: Int by Property(3, 0)
    protected var totalProcessTime: Int by Property(4, 0)
    private val usedRecipes = mutableMapOf<Identifier, Int>()

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val inputInventory = inventory.getInputInventory()
        if (isProcessing()) {
            val recipe = getCurrentRecipe()
            val upgrades = getUpgrades(inventory)
            if (!matchesRecipe(recipe, inputInventory))
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) {
                setWorkingState(true)
                processTime = (processTime - ceil(Upgrade.getSpeed(upgrades, this))).coerceAtLeast(0.0).toInt()
                if (processTime <= 0) {
                    val output = recipe?.craft(inventory) ?: return
                    inventory.inputSlots.forEachIndexed { index, slot ->
                        val stack = inputInventory.getStack(index)
                        val item = stack.item
                        if (
                            item.hasRecipeRemainder()
                            && !output.item.hasRecipeRemainder()
                            && item.recipeRemainder != output.item.recipeRemainder
                        )
                            inventory.setStack(slot, ItemStack(item.recipeRemainder))
                        else {
                            stack.decrement(1)
                            inventory.setStack(slot, stack)
                        }
                    }
                    for (outputSlot in inventory.outputSlots) {
                        val outputStack = inventory.getStack(outputSlot)
                        if (outputStack.item == output.item)
                            inventory.setStack(outputSlot, outputStack.apply { increment(output.count) })
                        else if (outputStack.isEmpty)
                            inventory.setStack(outputSlot, output)
                        else continue
                        break
                    }
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

    protected open fun matchesRecipe(recipe: T?, inventory: Inventory): Boolean = recipe?.matches(inventory, this.world) == true

    abstract fun tryStartRecipe(inventory: IRInventory): T?

    abstract fun getCurrentRecipe(): T?

    protected fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun isProcessing() = processTime > 0 && energy > 0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> getConfig().energyCost
        Upgrade.SPEED ->
            if (temperatureComponent?.isFullEfficiency() == true)
                getHeatConfig()?.processTemperatureBoost ?: getConfig().processSpeed
            else
                getConfig().processSpeed
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
    }

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

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

    abstract fun getConfig(): IConfig

    private fun getHeatConfig(): HeatMachineConfig? = getConfig() as? HeatMachineConfig

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