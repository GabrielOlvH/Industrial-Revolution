package me.steven.indrev.blockentities.modularworkbench

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.autosync
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.armor.IRColorModuleItem
import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.recipes.machines.ModuleRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.getRecipes
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class ModularWorkbenchBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MODULAR_WORKBENCH_REGISTRY, pos, state) {

    init {
        this.inventoryComponent = inventory(this) {

            maxStackCount = 1

            0 filter { (_, item) -> item !is IRModularItem<*> }
            1 filter { stack -> stack.item is IRModuleItem }
            2 filter { stack -> stack.item is IRModularItem<*> }
            3 until 15 filter { stack, index -> recipe != null && recipe!!.input.size > index - 3 && recipe!!.input[index - 3].ingredient.test(stack) }
            input {
                slots = (1 until 15).map { it }.toIntArray()
            }
            output { slot = 15 }
        }
    }

    override val syncToWorld: Boolean = true

    override val maxOutput: Long = 0

    var moduleProcessTime by autosync(PROCESS_TIME_ID, 0)
    var moduleMaxProcessTime by autosync(MAX_PROCESS_TIME_ID, 0)

    private var processTime by autosync(INSTALL_TIME_ID, 0)
    private var maxProcessTime by autosync(MAX_INSTALL_TIME_ID, 0)

    private var state: State by autosync(STATE_ID, State.IDLE, State.values())

    var selectedRecipe: Identifier? = null
    var recipe: ModuleRecipe? = null
        get() {
            if (selectedRecipe != null)
                field = world!!.recipeManager.getRecipes(ModuleRecipe.TYPE)[selectedRecipe]!!
            return field
        }

    override fun machineTick() {
        tickModuleInstall()
        tickModuleCraft()
    }

    private fun tickModuleCraft() {
        val inventory = inventoryComponent?.inventory ?: return
        if (!inventory.getStack(15).isEmpty) return
        val inputStacks = inventory.inputSlots.map { inventory.getStack(it) }
        when {
            recipe?.matches(inputStacks, emptyList()) != true -> {
                moduleMaxProcessTime = 0
                moduleProcessTime = 0
            }
            moduleMaxProcessTime in 1..moduleProcessTime -> {
                (3 until 15).forEach { slot -> inventory.setStack(slot, ItemStack.EMPTY) }
                inventory.setStack(15, recipe!!.outputs[0].stack.copy())

                moduleMaxProcessTime = 0
                moduleProcessTime = 0
            }
            else -> {
                moduleMaxProcessTime = recipe!!.ticks
                moduleProcessTime++
            }
        }
    }

    private fun tickModuleInstall() {
        val inventory = inventoryComponent?.inventory ?: return
        val targetStack = inventory.getStack(2)
        val moduleStack = inventory.getStack(1)
        if (moduleStack.item !is IRModuleItem || targetStack.item !is IRModularItem<*>) {
            processTime = 0
            state = State.IDLE
            return
        }
        val targetItem = targetStack.item as IRModularItem<*>
        val moduleItem = moduleStack.item as IRModuleItem
        val module = moduleItem.module
        val compatible = targetItem.getCompatibleModules(targetStack)
        if (inventory.isEmpty) {
            processTime = 0
            workingState = false
            state = State.IDLE
        } else {
            if (isProcessing()
                && compatible.contains(module)
                && use(config.energyCost)) {
                workingState = true
                processTime += config.processSpeed.toInt()
                if (processTime >= maxProcessTime) {
                    inventory.setStack(1, ItemStack.EMPTY)
                    val tag = targetStack.orCreateNbt
                    when {
                        module == ArmorModule.COLOR -> {
                            if (targetItem !is IRModularArmorItem) return
                            val colorModuleItem = moduleItem as IRColorModuleItem
                            targetItem.setColor(targetStack, colorModuleItem.color)
                        }
                        tag.contains(module.key) -> {
                            val level = tag.getInt(module.key) + 1
                            tag.putInt(module.key, level.coerceAtMost(module.maxLevel))
                        }
                        else -> tag.putInt(module.key, 1)
                    }
                    processTime = 0
                    state = State.IDLE
                }
            } else if (energy > 0 && !targetStack.isEmpty && !moduleStack.isEmpty && compatible.contains(module)) {
                val tag = targetStack.orCreateNbt
                if (tag.contains(module.key)) {
                    val level = module.getMaxInstalledLevel(targetStack)
                    if (module != ArmorModule.COLOR && level >= module.maxLevel) {
                        state = State.MAX_LEVEL
                        return
                    }
                }
                processTime = 1
                maxProcessTime = 1200
                workingState = true
                state = State.INSTALLING
            } else {
                state = State.INCOMPATIBLE
            }
        }
    }

    private fun isProcessing(): Boolean = processTime > 0 && energy > 0

    override fun fromTag(tag: NbtCompound) {
        processTime = tag.getInt("ProcessTime")
        if (tag.contains("SelectedRecipe"))
            selectedRecipe = Identifier(tag.getString("SelectedRecipe"))
        super.fromTag(tag)
    }

    override fun toTag(tag: NbtCompound) {
        tag.putInt("ProcessTime", processTime)
        if (selectedRecipe != null)
            tag.putString("SelectedRecipe", selectedRecipe!!.toString())
        super.toTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        if (tag.contains("SelectedRecipe"))
            selectedRecipe = Identifier(tag.getString("SelectedRecipe"))
        inventoryComponent!!.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        if (selectedRecipe != null)
            tag.putString("SelectedRecipe", selectedRecipe!!.toString())
        inventoryComponent!!.writeNbt(tag)
    }

    enum class State {
        IDLE,
        INSTALLING,
        INCOMPATIBLE,
        MAX_LEVEL;
    }

    companion object {
        const val PROCESS_TIME_ID = 2
        const val MAX_PROCESS_TIME_ID = 3
        const val INSTALL_TIME_ID = 4
        const val MAX_INSTALL_TIME_ID = 5
        const val STATE_ID = 6
    }
}