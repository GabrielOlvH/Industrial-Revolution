package me.steven.indrev.armor

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

enum class Module(
    val key: String,
    val slots: Array<EquipmentSlot>,
    val maxLevel: Int,
    val apply: (ServerPlayerEntity, ItemStack, Int) -> Boolean = { _, _, _ -> false }
) {
    NIGHT_VISION("night_vision", arrayOf(EquipmentSlot.HEAD), 1, { player, _, _ ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, 200, 0))
        true
    }),
    SPEED("speed", arrayOf(EquipmentSlot.LEGS), 3, { player, _, level ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 200, level - 1))
        true
    }),
    JUMP_BOOST("jump_boost", arrayOf(EquipmentSlot.FEET), 3, { player, _, level ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, level - 1))
        true
    }),
    BREATHING("breathing", arrayOf(EquipmentSlot.HEAD), 1, { player, _, level ->
        if (player.isSubmergedInWater) {
            player.addStatusEffect(StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, level - 1))
            true
        } else false
    }),
    PROTECTION("protection", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 3);

    companion object {
        fun getUpgrades(stack: ItemStack): Array<Module> {
            val tag = stack.tag ?: return emptyArray()
            return values().mapNotNull { upgrade ->
                if (tag.contains(upgrade.key)) upgrade
                else null
            }.toTypedArray()
        }

        fun getLevel(stack: ItemStack, upgrade: Module): Int {
            val tag = stack.tag ?: return 0
            return if (tag.contains(upgrade.key)) tag.getInt(upgrade.key) else 0
        }
    }
}