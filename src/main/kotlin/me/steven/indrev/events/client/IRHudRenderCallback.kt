package me.steven.indrev.events.client

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.api.IRPlayerEntityExtension
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.misc.IRMachineUpgradeItem
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot.*
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.hit.BlockHitResult

object IRHudRenderCallback : HudRenderCallback {
    override fun onHudRender(matrices: MatrixStack, tickDelta: Float) {
        renderModularArmorHud(matrices)
        renderTierUpgrade(matrices)
    }

    private fun renderModularArmorHud(matrices: MatrixStack) {
        val client = MinecraftClient.getInstance()
        val player = client.player
        if (player is IRPlayerEntityExtension && player.getMaxShieldDurability() > 0) {

            val color = player.inventory.getArmorStack(HEAD.entitySlotId).let { (stack, item) ->
                if ((item as? IRModularArmorItem)?.slotType != HEAD) -1
                else item.getColor(stack)
            }
            val x = IRConfig.hud.renderPosX + 2
            val y = IRConfig.hud.renderPosY + 2

            val spriteId = when {
                player.shieldDurability == player.getMaxShieldDurability() -> DEFAULT
                player.isRegenerating -> REGENERATING
                player.shieldDurability <= player.getMaxShieldDurability() * 0.25 -> WARNING
                else -> DAMAGED
            }
            val sprite = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(spriteId)
            texturedRect(x + 5, y + 50, 16, 16, sprite, color, 0.8f)

            ScreenDrawing.texturedRect(matrices, x, y, 90, 62, HUD_MAIN, color, 0.8f)
            ScreenDrawing.texturedRect(matrices, x + 7, y + 33, 83, 20, HOLDER, color, 0.3f)
            val shieldText = "${player.shieldDurability.toInt()}/${player.getMaxShieldDurability().toInt()}"
            ScreenDrawing.drawStringWithShadow(matrices, shieldText, HorizontalAlignment.CENTER, x + 20, y + 56, client.textRenderer.getWidth(shieldText), color)
            player.armorItems.forEach { stack ->
                val item = stack.item as? ArmorItem ?: return@forEach
                val xOffset = 21 * when (item.slotType) {
                    FEET -> 3
                    LEGS -> 2
                    CHEST -> 1
                    HEAD -> 0
                    else -> return@forEach
                }
                client.itemRenderer.renderInGui(stack, x + 9 + xOffset, y + 35)
                client.itemRenderer.renderGuiItemOverlay(client.textRenderer, stack, x + 9 + xOffset, y + 35)
            }
        }
    }

    private fun renderTierUpgrade(matrices: MatrixStack) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val player = client.player ?: return
        val hit = MinecraftClient.getInstance().crosshairTarget as? BlockHitResult ?: return
        val x = client.window.scaledWidth / 2
        val y = (client.window.scaledHeight / 2) + 8

        val stack = player.mainHandStack
        val item = stack.item as? IRMachineUpgradeItem ?: return
        val state = world.getBlockState(hit.blockPos)
        val block = state?.block as? MachineBlock

        client.itemRenderer.renderInGui(stack, x, y)

        if (block != null && block.registry.upgradeable && block.tier == item.from) {
            matrices.push()
            matrices.scale(0.5f, 0.5f, 0.5f)
            client.textRenderer.draw(matrices, "Right Click to upgrade machine", x.toFloat() + 16 + 64 + 64 + 64 + 42, y.toFloat()+ 64 + 64 + 13, -1)
            matrices.pop()

            client.itemRenderer.renderInGui(ItemStack(block), x + 8, y + 16)
            RenderSystem.disableDepthTest()
            ScreenDrawing.texturedRect(matrices, x + 8 + 16 + 2, y + 16, 16, 16, ARROW, -1)
            ScreenDrawing.texturedRect(matrices, x + 8 + 16 + 6, y + 18, 16, 16, CHECKMARK, -1)
            client.itemRenderer.renderInGui(ItemStack(block.registry.block(item.to)), x + 8 + 32 + 4, y + 16)
        } else {
            RenderSystem.disableDepthTest()
            ScreenDrawing.texturedRect(matrices, x + 5, y + 5, 16, 16, X, -1)
            RenderSystem.enableDepthTest()
            matrices.push()
            matrices.scale(0.5f, 0.5f, 0.5f)

            val txt = when {
                block == null -> "This is not a machine"
                !block.registry.upgradeable -> "This machine does not accept tier upgrades"
                block.tier != item.from -> "This is not the right tier"
                else -> "This should not happen!"
            }

            client.textRenderer.draw(matrices, txt,  x.toFloat() + 16 + 64 + 64 + 64 + 42, y.toFloat()+ 64 + 64 + 13, -1)
            matrices.pop()
        }

    }


    fun texturedRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        sprite: Sprite,
        color: Int,
        opacity: Float
    ) {
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderColor(r, g, b, opacity)
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        Tessellator.getInstance().run {
            buffer.run {
                begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
                vertex(x.toDouble(), y + height.toDouble(), -90.0).texture(sprite.minU, sprite.maxV).next()
                vertex(x + width.toDouble(), y + height.toDouble(), -90.0).texture(sprite.maxU, sprite.maxV).next()
                vertex(x + width.toDouble(), y.toDouble(), -90.0).texture(sprite.maxU, sprite.minV).next()
                vertex(x.toDouble(), y.toDouble(), -90.0).texture(sprite.minU, sprite.minV).next()
            }
            draw()
        }
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private val WARNING = identifier("gui/hud_warning")
    private val REGENERATING = identifier("gui/hud_regenerating")
    private val DAMAGED = identifier("gui/hud_damaged")
    private val DEFAULT = identifier("gui/hud_default")
    private val HUD_MAIN = identifier("textures/gui/hud_main.png")
    private val HOLDER = identifier("textures/gui/hud_armor_holder.png")

    private val CHECKMARK = identifier("textures/gui/checkmark.png")
    private val X = identifier("textures/gui/x.png")
    private val ARROW = identifier("textures/gui/widget_processing_empty.png")
}