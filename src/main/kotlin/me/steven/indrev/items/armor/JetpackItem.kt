package me.steven.indrev.items.armor

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.utils.itemSettings
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.times
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

class JetpackItem(tier: Tier) : ArmorItem(IRArmorMaterial.JETPACK, EquipmentSlot.CHEST, itemSettings().maxCount(1)), JetpackHandler {

    override val limit = FluidAmount.BUCKET.mul(10L * (tier.ordinal + 1))

    override val fluidFilter: FluidFilter = FluidFilter { it.rawFluid == IRFluidRegistry.HYDROGEN_STILL }

    private fun isActivated(stack: ItemStack) = stack.isOf(this) && stack.orCreateTag.getBoolean("Activated")

    fun toggle(stack: ItemStack) {
        if (stack.isOf(this)) {
            val tag = stack.orCreateTag
            tag.putBoolean("Activated", tag.getBoolean("Activated"))
        }
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val volume = getFuelStored(stack)
        return (13.0 - (((limit - volume.amount()) * 13) / limit).asInexactDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int = getFuelStored(stack).fluidKey.renderColor

    override fun isItemBarVisible(stack: ItemStack): Boolean = !getFuelStored(stack).isEmpty

}