package me.steven.indrev.items.armor

import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World
import kotlin.math.roundToInt

class JetpackItem(tier: Tier) : ArmorItem(IRArmorMaterial.JETPACK, EquipmentSlot.CHEST, itemSettings().maxCount(1)), JetpackHandler {

    init {
        FluidStorage.ITEM.registerForItems({ _, ctx -> JetpackHandler.JetpackFluidStorage(this, ctx) }, this)
    }

    override val capacity = bucket * (10L * (tier.ordinal + 1))

    override val fluidFilter: (FluidVariant) -> Boolean = { it.isOf(IRFluidRegistry.HYDROGEN_STILL) }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        val fuel = getFuelStored(stack)
        if (fuel.amount > 0) {
            tooltip.addAll(getTooltip(fuel.resource, fuel.amount, capacity))
            tooltip.add(EMPTY)
        }
        tooltip.add(
            translatable("item.indrev.jetpack.tooltip", literal("").append(
                IndustrialRevolutionClient.JETPACK_TOGGLE_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(
                Formatting.GRAY))
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val volume = getFuelStored(stack)
        return (13.0 - (((capacity - volume.amount()) * 13) / capacity).toDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int = FluidRenderHandlerRegistry.INSTANCE.get(getFuelStored(stack).resource.fluid).getFluidColor(null, null, getFuelStored(stack).resource.fluid.defaultState)

    override fun isItemBarVisible(stack: ItemStack): Boolean = getFuelStored(stack).amount > 0

}