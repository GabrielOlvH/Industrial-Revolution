package me.steven.indrev.blockentities.modularworkbench

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class ModularWorkbenchBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.MODULAR_WORKBENCH_REGISTRY) {

    init {
        this.inventoryController = InventoryController {
            IRInventory(3, EMPTY_INT_ARRAY, EMPTY_INT_ARRAY) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    slot == 1 || slot == 2 -> item is IRModularArmor || item is IRModuleItem
                    else -> false
                }
            }
        }
        this.propertyDelegate = ArrayPropertyDelegate(3)
    }

    private var processTime: Int by Property(2, 0)

    override fun machineTick() {
        val inventory = inventoryController?.inventory ?: return
        val armorStack = inventory.getStack(2)
        val moduleStack = inventory.getStack(1)
        if (armorStack.item !is IRModularArmor || moduleStack.item !is IRModuleItem) {
            processTime = 0
            return
        }
        val armorItem = armorStack.item as IRModularArmor
        val moduleItem = moduleStack.item as IRModuleItem
        val module = moduleItem.module
        if (inventory.isEmpty) {
            processTime = 0
            setWorkingState(false)
        } else if (isProcessing()
            && module.slots.contains(armorItem.slotType)
            && Energy.of(this).use(128.0)) {
            setWorkingState(true)
            processTime = (processTime - 1.0).coerceAtLeast(0.0).toInt()
            if (processTime <= 0) {
                inventory.setStack(1, ItemStack.EMPTY)
                val tag = armorStack.orCreateTag
                if (tag.contains(module.key)) {
                    val level = tag.getInt(module.key) + 1
                    tag.putInt(module.key, level.coerceAtMost(module.maxLevel))
                } else tag.putInt(module.key, 1)
                processTime = 0
            }
        } else if (energy > 0 && !armorStack.isEmpty && !moduleStack.isEmpty && processTime <= 0) {
            val tag = armorStack.orCreateTag
            if (tag.contains(module.key)) {
                val level = tag.getInt(module.key)
                if (level >= module.maxLevel) return
            }
            processTime = 1200
            setWorkingState(true)
        } else processTime = 0
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