package me.steven.indrev.items.armor

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import dev.emi.stepheightentityattribute.StepHeightEntityAttributeMain
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.AttributeModifierProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.items.energy.IREnergyItem
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.energyOf
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ArmorItem
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import java.util.*

class IRModularArmorItem(slot: EquipmentSlot, maxStored: Double, settings: Settings) :
    DyeableArmorItem(IRArmorMaterial.MODULAR, slot, settings), IRModularItem<ArmorModule>, AttributeModifierProvider, IREnergyItem {

    init {
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(maxStored, Tier.MK4.io, Tier.MK4.io), this)
    }

    override fun isEnchantable(stack: ItemStack?): Boolean = false

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        getInstalledTooltip(getInstalled(stack), stack, tooltip)
        buildEnergyTooltip(stack, tooltip)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getColor(stack: ItemStack?): Int {
        val compoundTag = stack!!.getSubTag("display")
        return if (compoundTag != null && compoundTag.contains("color", 99)) compoundTag.getInt("color") else -1
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
        val tag = stack.tag ?: return emptyList()
        return getCompatibleModules(stack).filter { module -> module != ArmorModule.COLOR }.mapNotNull { module ->
            if (tag.contains(module.key)) module
            else null
        }
    }

    override fun getAttributeModifiers(
        itemStack: ItemStack,
        equipmentSlot: EquipmentSlot
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val item = itemStack.item as IRModularArmorItem
        val itemIo = energyOf(itemStack)
        if (itemIo == null || itemIo.energy <= 0) {
            return ImmutableMultimap.of()
        } else if (equipmentSlot == item.slotType) {
            val level = ArmorModule.PROTECTION.getLevel(itemStack).toDouble()
            val uUID = MODIFIERS[equipmentSlot.entitySlotId]
            val attr = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>()
            if (level > 0) {
                val toughnessModifier = EntityAttributeModifier(uUID, "Armor toughness", item.method_26353() * level, EntityAttributeModifier.Operation.ADDITION)
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