package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.EMPTY
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import me.steven.indrev.utils.literal
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.world.World
import java.util.*

class IREnhancerItem(settings: Settings, val enhancer: Enhancer) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(translatable("item.indrev.${enhancer.toString().lowercase(Locale.getDefault())}_enhancer.tooltip").formatted(Formatting.GREEN))
        tooltip?.add(EMPTY)
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is IRInventoryScreen<*>) {
            val handler = currentScreen.screenHandler as? IRGuiScreenHandler ?: return
            handler.ctx.run { _, pos ->
                val blockEntity = world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                val enhancerComponent = blockEntity.enhancerComponent ?: return@run
                if (!enhancerComponent.compatible.contains(enhancer))
                    tooltip?.add(translatable("item.indrev.enhancers.incompatible").formatted(Formatting.DARK_RED))
                else
                    tooltip?.add(translatable("item.indrev.enhancers.count", enhancerComponent.maxSlotCount(enhancer)).formatted(Formatting.AQUA))
            }
        }
    }
}