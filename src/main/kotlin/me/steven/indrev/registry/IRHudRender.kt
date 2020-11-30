package me.steven.indrev.registry

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.IRPlayerEntityExtension
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.text.LiteralText

object IRHudRender : HudRenderCallback {

    init {
        HudRenderCallback.EVENT.register(this)
    }

    override fun onHudRender(matrixStack: MatrixStack?, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        val x = IndustrialRevolution.CONFIG.hud.renderPosX
        val y = IndustrialRevolution.CONFIG.hud.renderPosY
        val player = MinecraftClient.getInstance().player
        val armor = player?.inventory?.armor?.filter { it.item is IRModularArmor }
        armor?.forEach { itemStack ->
            val item = itemStack.item as IRModularArmor
            val yOffset = when (item.slotType) {
                EquipmentSlot.HEAD -> 0
                EquipmentSlot.CHEST -> 20
                EquipmentSlot.LEGS -> 20 * 2
                EquipmentSlot.FEET -> 20 * 3
                else -> return@forEach
            }
            if (shouldRenderShield(item.slotType)) {
                val totalShield = item.getMaxShield(ArmorModule.PROTECTION.getLevel(itemStack))
                if (totalShield > 0) {
                    // TODO fix this hud
                    val currentShield = 0.0//item.getShield(itemStack)
                    var percent = currentShield.toFloat() / totalShield.toFloat()
                    val color = if (percent < 0.35) 0xff0000 else -1
                    val height = 16
                    val width = 16
                    percent = (percent * height).toInt() / height.toFloat()
                    val barSize = (height * percent).toInt()
                    ScreenDrawing.texturedRect(x + 18, y + yOffset + 1, width, height, SHIELD_ICON_FULL, 0f, 0f, 1f, 1f, color, 0.5f)
                    if (barSize > 0)
                        ScreenDrawing.texturedRect(
                            x + 18, y + yOffset + height - barSize + 1, width, barSize,
                            SHIELD_ICON_FULL, 0f, 1 - percent, 1f, 1f, color
                        )
                }
            }
            if (shouldRenderArmor(item.slotType)) {
                val armorIcon = when (item.slotType) {
                    EquipmentSlot.HEAD -> HELMET_ICON
                    EquipmentSlot.CHEST -> CHEST_ICON
                    EquipmentSlot.LEGS -> LEGS_ICON
                    EquipmentSlot.FEET -> BOOTS_ICON
                    else -> return@forEach
                }
                ScreenDrawing.texturedRect(x, y + yOffset, 16, 16, armorIcon, 0f, 0f, 1f, 1f, -1, 0.8f)
                client.itemRenderer.renderGuiItemOverlay(client.textRenderer, itemStack, x, y + yOffset)
            }
        }
        if (player is IRPlayerEntityExtension) {
            val literalText = LiteralText("Shield: ${player.shieldDurability} / ${player.getMaxShieldDurability()}")
            client.textRenderer.draw(matrixStack, literalText, x.toFloat(), y.toFloat(), 0xFFFFFFFF.toInt())
        }
    }

    private fun shouldRenderArmor(equipmentSlot: EquipmentSlot): Boolean {
        val hud = IndustrialRevolution.CONFIG.hud
        return when (equipmentSlot) {
            EquipmentSlot.HEAD -> hud.renderHelmetArmor
            EquipmentSlot.CHEST -> hud.renderChestplateArmor
            EquipmentSlot.LEGS -> hud.renderLeggingsArmor
            EquipmentSlot.FEET -> hud.renderBootsArmor
            else -> false
        }
    }

    private fun shouldRenderShield(equipmentSlot: EquipmentSlot): Boolean {
        val hud = IndustrialRevolution.CONFIG.hud
        return when (equipmentSlot) {
            EquipmentSlot.HEAD -> hud.renderHelmetShield
            EquipmentSlot.CHEST -> hud.renderChestplateShield
            EquipmentSlot.LEGS -> hud.renderLeggingsShield
            EquipmentSlot.FEET -> hud.renderBootsShield
            else -> false
        }
    }

    private val SHIELD_ICON_FULL = identifier("textures/gui/shield_icon.png")
    private val HELMET_ICON = identifier("textures/item/modular_armor_helmet.png")
    private val CHEST_ICON = identifier("textures/item/modular_armor_chest.png")
    private val LEGS_ICON = identifier("textures/item/modular_armor_legs.png")
    private val BOOTS_ICON = identifier("textures/item/modular_armor_boots.png")
}