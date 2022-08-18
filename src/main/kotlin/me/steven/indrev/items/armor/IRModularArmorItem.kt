package me.steven.indrev.items.armor

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import dev.emi.stepheightentityattribute.StepHeightEntityAttributeMain
import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.AttributeModifierProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.gui.tooltip.modular.ModularTooltipData
import me.steven.indrev.items.energy.IREnergyItem
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ArmorItem
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl
import java.util.*
import kotlin.math.roundToInt

class IRModularArmorItem(slot: EquipmentSlot, maxStored: Long, settings: Settings) :
    DyeableArmorItem(IRArmorMaterial.MODULAR, slot, settings), IRModularItem<ArmorModule>, AttributeModifierProvider, IREnergyItem, JetpackHandler, FabricElytraItem {

    init {
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, maxStored, Tier.MK4.io, Tier.MK4.io) }, this)
    }

    override val fluidFilter: (FluidVariant) -> Boolean = { it.isOf(IRFluidRegistry.HYDROGEN_STILL) }

    override val capacity: Long = bucket * 10

    override fun isUsable(stack: ItemStack): Boolean = ArmorModule.JETPACK.getLevel(stack) > 0

    fun getFluidItemBarStep(stack: ItemStack): Int {
        val volume = getFuelStored(stack)
        return (13.0 - (((capacity - volume.amount()) * 13) / capacity)).roundToInt()
    }

    fun getFluidItemBarColor(stack: ItemStack): Int = FluidRenderHandlerRegistry.INSTANCE.get(getFuelStored(stack).resource.fluid).getFluidColor(null, null, null)

    fun isFluidItemBarVisible(stack: ItemStack): Boolean = getFuelStored(stack).amount > 0

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun isEnchantable(stack: ItemStack?): Boolean = false

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        if (Screen.hasShiftDown()) {
            getInstalledTooltip(getInstalled(stack), stack, tooltip)
        }
        val fuel = getFuelStored(stack)
        if (fuel.amount > 0) {
            tooltip?.add(EMPTY)
            tooltip?.addAll(getTooltip(fuel.resource, fuel.amount, capacity))
            tooltip?.add(EMPTY)
        }
        tooltip?.add(
            translatable("item.indrev.modular_item.tooltip", literal("").append(
                IndustrialRevolutionClient.MODULAR_CONTROLLER_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(
                Formatting.GRAY))
        if (ArmorModule.JETPACK.isInstalled(stack))
            tooltip?.add(
                translatable("item.indrev.jetpack.tooltip", literal("").append(
                    IndustrialRevolutionClient.JETPACK_TOGGLE_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(
                    Formatting.GRAY))
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getColor(stack: ItemStack?): Int {
        val nbt = stack!!.getSubNbt("display")
        return if (nbt != null && nbt.contains("color", 99)) nbt.getInt("color") else -1
    }

    fun getMaxShield(protectionLevel: Int) = protectionLevel * 100.0

    override fun getCompatibleModules(itemStack: ItemStack): Array<ArmorModule> {
        val armor = itemStack.item as? ArmorItem ?: return emptyArray()
        return when (armor.slotType) {
            EquipmentSlot.HEAD -> ArmorModule.COMPATIBLE_HELMET
            EquipmentSlot.CHEST -> ArmorModule.COMPATIBLE_CHEST
            EquipmentSlot.LEGS -> ArmorModule.COMPATIBLE_LEGS
            EquipmentSlot.FEET -> ArmorModule.COMPATIBLE_BOOTS
            else -> return emptyArray()
        }
    }

    override fun getInstalled(stack: ItemStack): List<ArmorModule> {
        val tag = stack.nbt ?: return emptyList()
        return getCompatibleModules(stack).filter { module -> module != ArmorModule.COLOR }.mapNotNull { module ->
            if (tag.contains(module.key)) module
            else null
        }
    }

    override fun useCustomElytra(entity: LivingEntity, chestStack: ItemStack, tickElytra: Boolean): Boolean {
        if (ReinforcedElytraItem.canFallFly(chestStack)) {
            if (tickElytra) {
                doVanillaElytraTick(entity, chestStack)
            }
            return true
        }

        return false
    }

    override fun getAttributeModifiers(
        itemStack: ItemStack,
        equipmentSlot: EquipmentSlot
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val item = itemStack.item as IRModularArmorItem
        val itemIo = energyOf(itemStack)
        if (itemIo == null || itemIo.amount <= 0) {
            return ImmutableMultimap.of()
        } else if (equipmentSlot == item.slotType) {
            val level = ArmorModule.PROTECTION.getLevel(itemStack).toDouble()
            val uUID = MODIFIERS[equipmentSlot.entitySlotId]
            val attr = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>()
            if (level > 0) {
                val toughnessModifier = EntityAttributeModifier(uUID, "Armor toughness", item.toughness * level, EntityAttributeModifier.Operation.ADDITION)
                attr.put(
                    EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                    toughnessModifier
                )
                val armorModifier = EntityAttributeModifier(uUID, "Armor modifier", item.protection * level, EntityAttributeModifier.Operation.ADDITION)
                attr.put(
                    EntityAttributes.GENERIC_ARMOR,
                    armorModifier
                )
            }
            val speedLevel = ArmorModule.SPEED.getLevel(itemStack) * 0.9
            if (speedLevel > 0) {
                attr.put(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED,
                    EntityAttributeModifier(SPEED_MODIFIER, "Speed", speedLevel, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                )

                attr.put(
                    StepHeightEntityAttributeMain.STEP_HEIGHT,
                    EntityAttributeModifier(STEP_HEIGHT_MODIFIER, "Step-Height", 1.0, EntityAttributeModifier.Operation.ADDITION)
                )
            }
            return attr.build()
        }
        return getAttributeModifiers(equipmentSlot)
    }

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        val modules = getInstalled(stack)
        return Optional.of(ModularTooltipData(handler.amount, handler.capacity, modules) { it.getLevel(stack) })
    }

    companion object {
        private val MODIFIERS = arrayOf(
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
        )
        private val SPEED_MODIFIER = UUID.randomUUID()
        private val STEP_HEIGHT_MODIFIER = UUID.randomUUID()
    }
}