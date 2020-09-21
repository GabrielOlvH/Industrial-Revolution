package me.steven.indrev.armor

import me.steven.indrev.tools.Module
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class ArmorModule(
    override val key: String,
    val slots: Array<EquipmentSlot>,
    override val maxLevel: Int,
    val hasTexture: Boolean,
    val hasOverlay: Boolean,
    val apply: (ServerPlayerEntity, Int) -> StatusEffectInstance? = { _, _ -> null },
    override val isValid: (ItemStack) -> Boolean = { itemStack -> itemStack.item is ArmorItem && slots.contains((itemStack.item as ArmorItem).slotType) }
) : Module {
    NIGHT_VISION("night_vision", arrayOf(EquipmentSlot.HEAD), 1, true, true, { _, _ ->
        StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000000, 0, false, false)
    }),
    SPEED("speed", arrayOf(EquipmentSlot.LEGS), 3, false, false, { _, level ->
        StatusEffectInstance(StatusEffects.SPEED, 1000000, level - 1, false, false)
    }),
    JUMP_BOOST("jump_boost", arrayOf(EquipmentSlot.FEET), 3, false, false, { _, level ->
        StatusEffectInstance(StatusEffects.JUMP_BOOST, 1000000, level - 1, false, false)
    }),
    BREATHING("breathing", arrayOf(EquipmentSlot.HEAD), 1, false, false, { player, level ->
        if (player.isSubmergedInWater)
            StatusEffectInstance(StatusEffects.WATER_BREATHING, 1000000, level - 1, false, false)
        else {
            player.removeStatusEffect(StatusEffects.WATER_BREATHING)
            null
        }
    }),
    FEATHER_FALLING("feather_falling", arrayOf(EquipmentSlot.FEET), 1, false, false),
    PROTECTION(
        "protection",
        arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
        3,
        true,
        true
    ),
    AUTO_FEEDER("auto_feeder", arrayOf(EquipmentSlot.HEAD), 1, false, false),
    CHARGER("charger", arrayOf(EquipmentSlot.CHEST), 1, false, false),
    SOLAR_PANEL("solar_panel", arrayOf(EquipmentSlot.HEAD), 2, false, false),
    FIRE_RESISTANCE("fire_resistance", arrayOf(EquipmentSlot.CHEST), 1, false, false, { _, level ->
        StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1000000, level - 1, false, false)
    }),
    PIGLIN_TRICKER("piglin_tricker", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 1, false, false),
    COLOR("color", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), -1, false, false);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        slots.forEach {
            tooltip?.add(TranslatableText("item.indrev.module_parts_${it.toString().toLowerCase()}").formatted(Formatting.GOLD))
        }
    }

    companion object {
        fun isInstalled(stack: ItemStack, upgrade: ArmorModule): Boolean = stack.tag?.contains(upgrade.key) == true

        fun getInstalled(stack: ItemStack): Array<ArmorModule> {
            val tag = stack.tag ?: return emptyArray()
            return values().filter { module -> module != COLOR }.mapNotNull { module ->
                if (tag.contains(module.key)) module
                else null
            }.toTypedArray()
        }

        fun getLevel(stack: ItemStack, upgrade: ArmorModule): Int {
            val tag = stack.tag ?: return 0
            return if (tag.contains(upgrade.key)) tag.getInt(upgrade.key) else 0
        }
    }
}