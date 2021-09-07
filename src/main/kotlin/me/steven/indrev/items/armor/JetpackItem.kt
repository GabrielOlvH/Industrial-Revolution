package me.steven.indrev.items.armor

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

class JetpackItem(tier: Tier) : ArmorItem(IRArmorMaterial.JETPACK, EquipmentSlot.CHEST, itemSettings().maxCount(1)), JetpackHandler {

    init {
    }

    override val limit = bucket * (10L * (tier.ordinal + 1))

    override val fluidFilter: (FluidVariant) -> Boolean ={ it.isOf(IRFluidRegistry.HYDROGEN_STILL) }

    private fun isActivated(stack: ItemStack) = stack.isOf(this) && stack.orCreateNbt.getBoolean("Activated")

    fun toggle(stack: ItemStack) {
        if (stack.isOf(this)) {
            val tag = stack.orCreateNbt
            tag.putBoolean("Activated", tag.getBoolean("Activated"))
        }
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val volume = getFuelStored(stack)
        return (13.0 - (((limit - volume.amount()) * 13) / limit).toDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int = FluidRenderHandlerRegistry.INSTANCE.get(getFuelStored(stack).resource.fluid).getFluidColor(null, null, getFuelStored(stack).resource.fluid.defaultState)

    override fun isItemBarVisible(stack: ItemStack): Boolean = getFuelStored(stack).amount > 0

}