package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.utils.energyOf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class IRPortableChargerItem(
    settings: Settings,
    maxStored: Double
) : Item(settings), IREnergyItem {

    init {
        EnergyApi.ITEM.registerForItems(SimpleItemEnergyIo.getProvider(maxStored, 16384.0, 16384.0), this)
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {

        val player = entity as? PlayerEntity ?: return
        if (player.offHandStack != stack && player.mainHandStack != stack) return
        val items = (0 until player.inventory.size())
            .map { s -> player.inventory.getStack(s) }
            .filter { s -> s.item !is IRPortableChargerItem }
            .mapNotNull { s -> energyOf(s) }
        var rem = 16384.0
        items.forEach { h ->
            if (rem <= 0) return
            rem -= EnergyMovement.move(energyOf(stack) ?: return, h, rem)
        }
    }

    companion object {
        fun chargeItemsInInv(itemStack: ItemStack, inventory: DefaultedList<ItemStack>) {
            val items = (0 until inventory.size)
                .map { s -> inventory[s] }
                .filter { s -> s.item !is IRPortableChargerItem }
                .mapNotNull { s -> energyOf(s) }
            var rem = 16384.0
            items.forEach { h ->
                if (rem <= 0) return
                rem -= EnergyMovement.move(energyOf(itemStack) ?: return, h, rem)
            }
        }
    }
}