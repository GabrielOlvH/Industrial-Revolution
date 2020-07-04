package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.Property
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.inventory.Inventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Recipe
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.Tickable
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import kotlin.math.ceil

abstract class CraftingMachineBlockEntity<T : Recipe<Inventory>>(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity(tier, registry), Tickable, UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    protected var processTime: Int by Property(3, 0)
    protected var totalProcessTime: Int by Property(4, 0)

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = inventoryController?.getInventory() ?: return
        val inputInventory = inventory.getInputInventory()
        if (inputInventory.isEmpty) {
            reset()
            setWorkingState(false)
        } else if (isProcessing()) {
            val recipe = getCurrentRecipe()
            if (recipe?.matches(inputInventory, this.world) == false)
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(this).use(Upgrade.ENERGY.apply(this, inventory))) {
                setWorkingState(true)
                processTime = (processTime - ceil(Upgrade.SPEED.apply(this, inventory))).coerceAtLeast(0.0).toInt()
                if (processTime <= 0) {
                    inventory.inputSlots.forEachIndexed { index, slot ->
                        inventory.setStack(slot, inputInventory.getStack(index).apply { decrement(1) })
                    }
                    val output = recipe?.output ?: return
                    for (outputSlot in inventory.outputSlots) {
                        val outputStack = inventory.getStack(outputSlot)
                        if (outputStack.item == output.item)
                            inventory.setStack(outputSlot, outputStack.apply { increment(output.count) })
                        else if (outputStack.isEmpty)
                            inventory.setStack(outputSlot, output.copy())
                        else continue
                        break
                    }
                    onCraft()
                    reset()
                }
            } else reset()
        } else if (energy > 0 && !inputInventory.isEmpty && processTime <= 0) {
            reset()
            if (tryStartRecipe(inventory) == null) setWorkingState(false)
        }
        temperatureController?.tick(isProcessing())
        update()
    }

    abstract fun tryStartRecipe(inventory: IRInventory): T?

    abstract fun getCurrentRecipe(): T?

    private fun reset() {
        processTime = 0
        totalProcessTime = 0
    }

    override fun getMaxStoredPower(): Double = Upgrade.BUFFER.apply(this, inventoryController!!.getInventory())

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun isProcessing() = processTime > 0 && energy > 0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 4.0 * tier.ordinal * Upgrade.SPEED.apply(this, inventoryController!!.getInventory())
        Upgrade.SPEED -> if (temperatureController?.isFullEfficiency() == true) 2.0 else 1.0
        Upgrade.BUFFER -> baseBuffer
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

    open fun onCraft() {}
}