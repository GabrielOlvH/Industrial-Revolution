package me.steven.indrev.armor

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

enum class ModularUpgrade(val key: String, val slots: Array<EquipmentSlot>, val maxLevel: Int, val apply: (ServerPlayerEntity, ItemStack, Int) -> Unit) {
    NIGHT_VISION("night_vision", arrayOf(EquipmentSlot.HEAD), 1, { player, _, _ ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, 200, 0))
    }),
    SPEED("speed", arrayOf(EquipmentSlot.LEGS), 3, { player, _, level ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 200, level))
    }),
    RESISTANCE("resistance", arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET), 3, { player, _, level ->
        player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 200, level))
    });

    companion object {
        fun getUpgrades(stack: ItemStack): Array<ModularUpgrade> {
            val tag = stack.tag ?: return emptyArray()
            return values().mapNotNull { upgrade ->
                if (tag.contains(upgrade.key)) upgrade
                else null
            }.toTypedArray()
        }

        fun getLevel(stack: ItemStack, upgrade: ModularUpgrade): Int {
            val tag = stack.tag ?: return -1
            return if (tag.contains(upgrade.key)) tag.getInt(upgrade.key) else -1
        }
    }
}