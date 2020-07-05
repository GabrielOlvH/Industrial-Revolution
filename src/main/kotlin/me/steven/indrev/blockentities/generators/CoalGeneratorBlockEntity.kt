package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class CoalGeneratorBlockEntity :
    GeneratorBlockEntity(Tier.MK1, MachineRegistry.COAL_GENERATOR_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryController = InventoryController {
            IRInventory(3, intArrayOf(2), EMPTY_INT_ARRAY) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot == 2 -> BURN_TIME_MAP.containsKey(stack?.item)
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController({ this }, 0.08, 900..2000, 2500.0)
    }

    private var burnTime: Int by Property(3, 0)
    private var maxBurnTime: Int by Property(4, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val inventory = inventoryController?.getInventory() ?: return false
            val invStack = inventory.getStack(2)
            if (!invStack.isEmpty && BURN_TIME_MAP.containsKey(invStack.item)) {
                burnTime = BURN_TIME_MAP[invStack.item] ?: return false
                maxBurnTime = burnTime
                invStack.count--
                if (invStack.isEmpty) inventory.setStack(2, ItemStack.EMPTY)
                else inventory.setStack(2, invStack)
            }
        }
        markDirty()
        return burnTime > 0 && energy < maxStoredPower
    }

    override fun getGenerationRatio(): Double = 16.0

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.toClientTag(tag)
    }

    companion object {
        private val BURN_TIME_MAP = AbstractFurnaceBlockEntity.createFuelTimeMap().also {
            if (Registry.ITEM.containsId(Identifier("c:coal_coke"))) {
                it[Registry.ITEM[Identifier("c:coal_coke")]] = 2000
            }
            if (Registry.ITEM.containsId(Identifier("c:coal_coke_block"))) {
                it[Registry.ITEM[Identifier("c:coal_coke_block")]] = 2000 * 9
            }
        }
    }
}