package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.ContainerItemContext
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import dev.technici4n.fasttransferlib.api.item.ItemKey
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.energyOf
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class IRPortableChargerItem(
    settings: Settings,
    maxStored: Double
) : Item(settings), IREnergyItem {

    init {
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(maxStored, 16384.0, 16384.0), this)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        buildEnergyTooltip(stack, tooltip)
    }

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val handler = energyOf(stack) ?: return

        val player = entity as? PlayerEntity ?: return
        if (player.offHandStack != stack && player.mainHandStack != stack) return
        val items = (0 until player.inventory.size())
            .map { s -> player.inventory.getStack(s) }
            .filter { s -> s.item !is IRPortableChargerItem }
            .mapNotNull { s -> EnergyApi.ITEM[ItemKey.of(s), ContainerItemContext.ofStack(s)]}
        var rem = 16384.0
        items.forEach { h ->
            rem -= EnergyMovement.move(handler, h, rem)
        }
    }

    companion object {
        fun chargeItemsInInv(handler: EnergyIo, inventory: DefaultedList<ItemStack>) {
            val items = (0 until inventory.size)
                .map { s -> inventory[s] }
                .filter { s -> s.item !is IRPortableChargerItem }
                .mapNotNull { s -> EnergyApi.ITEM[ItemKey.of(s), ContainerItemContext.ofStack(s)]}
            var rem = 16384.0
            items.forEach { h ->
                rem -= EnergyMovement.move(handler, h, rem)
            }
        }
    }
}