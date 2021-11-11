package me.steven.indrev.items.energy

import me.steven.indrev.gui.tooltip.energy.EnergyTooltipData
import me.steven.indrev.utils.energyOf
import net.minecraft.client.item.TooltipData
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl
import java.util.*

class IRPortableChargerItem(
    settings: Settings,
    maxStored: Long
) : Item(settings), IREnergyItem {

    init {
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, maxStored, 16384, 16384) }, this)
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val player = entity as? PlayerEntity ?: return
        if (player.offHandStack != stack && player.mainHandStack != stack) return
        chargeItemsInInv(slot, player.inventory)
    }

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        return Optional.of(EnergyTooltipData(handler.amount, handler.capacity))
    }

    companion object {
        fun chargeItemsInInv(slot: Int, inventory: Inventory) {
            val items = (0 until inventory.size())
                .filter { s -> s != slot }
                .mapNotNull { s -> energyOf(inventory, s) }
            var rem = 16384L
            items.forEach { h ->
                if (rem <= 0) return
                rem -= EnergyStorageUtil.move(energyOf(inventory, slot) ?: return, h, rem, null)
            }
        }
    }
}