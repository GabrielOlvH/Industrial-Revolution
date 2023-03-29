package me.steven.indrev.tools.modular

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import me.steven.indrev.utils.literal
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting

enum class ArmorModule(
    override val key: String,
    val slots: Array<EquipmentSlot>,
    override val maxLevel: Int,
    override val item: ItemConvertible,
    val hasTexture: Boolean,
    val hasOverlay: Boolean,
) : Module {
    NIGHT_VISION("night_vision", arrayOf(EquipmentSlot.HEAD), 1, { IRItemRegistry.NIGHT_VISION_MODULE_ITEM }, true, true),
    SPEED("speed", arrayOf(EquipmentSlot.LEGS), 3, { IRItemRegistry.SPEED_MODULE_ITEM }, false, false),
    JUMP_BOOST("jump_boost", arrayOf(EquipmentSlot.FEET), 3, { IRItemRegistry.JUMP_BOOST_MODULE_ITEM },false, false),
    BREATHING("breathing", arrayOf(EquipmentSlot.HEAD), 1, { IRItemRegistry.BREATHING_MODULE_ITEM }, false, false),
    FEATHER_FALLING("feather_falling", arrayOf(EquipmentSlot.FEET), 1, { IRItemRegistry.FEATHER_FALLING_MODULE_ITEM }, false, false),
    PROTECTION("protection", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 3, { IRItemRegistry.PROTECTION_MODULE_ITEM }, true, true),
    AUTO_FEEDER("auto_feeder", arrayOf(EquipmentSlot.HEAD), 1, { IRItemRegistry.AUTO_FEEDER_MODULE_ITEM }, false, false),
    CHARGER("charger", arrayOf(EquipmentSlot.CHEST), 1, { IRItemRegistry.CHARGER_MODULE_ITEM }, false, false),
    SOLAR_PANEL("solar_panel", arrayOf(EquipmentSlot.HEAD), 2, { IRItemRegistry.SOLAR_PANEL_MODULE_ITEM }, false, false),
    FIRE_RESISTANCE("fire_resistance", arrayOf(EquipmentSlot.CHEST),1, { IRItemRegistry.FIRE_RESISTANCE_MODULE_ITEM }, false, false),
    PIGLIN_TRICKER("piglin_tricker", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 1, { IRItemRegistry.PIGLIN_TRICKER_MODULE_ITEM },false, false),
    ELYTRA("elytra", arrayOf(EquipmentSlot.CHEST), 1, { IRItemRegistry.ELYTRA_MODULE_ITEM }, false, false),
    MAGNET("magnet", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 1, { IRItemRegistry.MAGNET_MODULE }, false, false),
    JETPACK("jetpack", arrayOf(EquipmentSlot.CHEST), 1, { IRItemRegistry.JETPACK_MODULE_ITEM }, false, false),
    WATER_AFFINITY("water_affinity", arrayOf(EquipmentSlot.CHEST, EquipmentSlot.LEGS), 1, { IRItemRegistry.WATER_AFFINITY_MODULE }, false, false) {
        override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
            val chestplate = translatable("item.indrev.module_water_affinity.on", translatable("item.indrev.module_water_affinity.chestplate").formatted(Formatting.GOLD))
            tooltip?.add(chestplate.formatted(Formatting.BLUE, Formatting.ITALIC))
            tooltip?.add(literal("   ")
                .append(translatable("item.indrev.module_water_affinity.tooltip")
                    .formatted(Formatting.BLUE, Formatting.ITALIC)))
            val legs = translatable("item.indrev.module_water_affinity.on", translatable("item.indrev.module_water_affinity.leggings").formatted(Formatting.GOLD))
            tooltip?.add(legs.formatted(Formatting.BLUE, Formatting.ITALIC))
            tooltip?.add(literal("   ")
                .append(translatable("item.indrev.module_water_affinity.tooltip1")
                    .formatted(Formatting.BLUE, Formatting.ITALIC)))
            tooltip?.add(literal(" "))
            if (Screen.hasShiftDown()) {
                val maxLevelText = translatable(
                    "item.indrev.module_max_level",
                    literal(maxLevel.toString()).formatted(Formatting.GOLD)
                )
                tooltip?.add(maxLevelText.formatted(Formatting.BLUE))
            }

            tooltip?.add(translatable("item.indrev.module_parts").formatted(Formatting.BLUE))
            slots.forEach {
                tooltip?.add(translatable("item.indrev.module_parts_${it.toString().lowercase()}").formatted(Formatting.GOLD))
            }
        }
                                                                                                                                                  },
    COLOR("color", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), -1, { null },false, false);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(translatable("item.indrev.module_parts").formatted(Formatting.BLUE))
        slots.forEach {
            tooltip?.add(translatable("item.indrev.module_parts_${it.toString().lowercase()}").formatted(Formatting.GOLD))
        }
    }

    companion object {
        val COMPATIBLE: Array<ArmorModule> = values()
        val COMPATIBLE_HELMET: Array<ArmorModule> = COMPATIBLE.filter { it.slots.contains(EquipmentSlot.HEAD) }.toTypedArray()
        val COMPATIBLE_CHEST: Array<ArmorModule> = COMPATIBLE.filter { it.slots.contains(EquipmentSlot.CHEST) }.toTypedArray()
        val COMPATIBLE_LEGS: Array<ArmorModule> = COMPATIBLE.filter { it.slots.contains(EquipmentSlot.LEGS) }.toTypedArray()
        val COMPATIBLE_BOOTS: Array<ArmorModule> = COMPATIBLE.filter { it.slots.contains(EquipmentSlot.FEET) }.toTypedArray()
    }
}