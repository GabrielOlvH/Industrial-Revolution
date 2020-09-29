package me.steven.indrev.blockentities.modularworkbench

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.armor.IRColorModuleItem
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.Property
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class ModularWorkbenchBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.MODULAR_WORKBENCH_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(4)
        this.inventoryComponent = inventory(this) {
            0 filter { (stack, item) -> item !is IRModularArmor && Energy.valid(stack) && Energy.of(stack).maxOutput > 0 }
            1 filter { stack -> stack.item is IRModuleItem }
            2 filter { stack -> stack.item is IRModularItem<*> }
        }
    }

    private var processTime: Int by Property(2, 0)
    private var maxProcessTime: Int by Property(3, 0)

    override fun machineTick() {
        val inventory = inventoryComponent?.inventory ?: return
        val targetStack = inventory.getStack(2)
        val moduleStack = inventory.getStack(1)
        if (moduleStack.item !is IRModuleItem || targetStack.item !is IRModularItem<*>) {
            processTime = 0
            return
        }
        //val armorItem = armorStack.item as IRModularArmor
        val targetItem = targetStack.item as IRModularItem<*>
        val moduleItem = moduleStack.item as IRModuleItem
        val module = moduleItem.module
        val compatible = targetItem.getCompatibleModules(targetStack)
        if (inventory.isEmpty) {
            processTime = 0
            setWorkingState(false)
        } else {
            if (isProcessing()
                && compatible.contains(module)
                && Energy.of(this).use(config.energyCost)) {
                setWorkingState(true)
                processTime += config.processSpeed.toInt()
                if (processTime >= maxProcessTime) {
                    inventory.setStack(1, ItemStack.EMPTY)
                    val tag = targetStack.orCreateTag
                    when {
                        module == ArmorModule.COLOR -> {
                            if (targetItem !is IRModularArmor) return
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
                }
            } else if (energy > 0 && !targetStack.isEmpty && !moduleStack.isEmpty && compatible.contains(module)) {
                val tag = targetStack.orCreateTag
                if (tag.contains(module.key)) {
                    val level = module.getMaxInstalledLevel(targetStack)
                    if (module != ArmorModule.COLOR && level >= module.maxLevel) return
                }
                processTime = 1
                maxProcessTime = 1200
                setWorkingState(true)
            } else processTime = -1
        }
    }

    private fun isProcessing(): Boolean = processTime > 0 && energy > 0

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        super.fromTag(state, tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        processTime = tag?.getInt("ProcessTime") ?: 0
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("ProcessTime", processTime)
        return super.toClientTag(tag)
    }
}