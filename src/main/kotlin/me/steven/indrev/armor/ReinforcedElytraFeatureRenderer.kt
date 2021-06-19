package me.steven.indrev.armor

import me.steven.indrev.items.armor.ReinforcedElytraItem
import me.steven.indrev.utils.identifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.ElytraEntityModel
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.EntityModelLoader
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity

@Environment(EnvType.CLIENT)
class ReinforcedElytraFeatureRenderer<T : LivingEntity, M : EntityModel<T>>(
    context: FeatureRendererContext<T, M>,
    loader: EntityModelLoader
) : FeatureRenderer<T, M>(context) {

    private val elytraModel: ElytraEntityModel<T> = ElytraEntityModel(loader.getModelPart(EntityModelLayers.ELYTRA))

    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        val itemStack = entity.getEquippedStack(EquipmentSlot.CHEST)
        if (ReinforcedElytraItem.hasValidElytra(itemStack)) {
            val textureId = when {
                entity !is AbstractClientPlayerEntity -> REINFORCED_ELYTRA_SKIN
                entity.canRenderElytraTexture() && entity.elytraTexture != null -> entity.elytraTexture!!
                entity.canRenderCapeTexture() && entity.capeTexture != null && entity.isPartVisible(PlayerModelPart.CAPE) -> entity.capeTexture!!
                else -> REINFORCED_ELYTRA_SKIN
            }
            matrices.push()
            matrices.translate(0.0, 0.0, 0.125)
            this.contextModel.copyStateTo(elytraModel)
            elytraModel.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch)
            val vertexConsumer = ItemRenderer.getArmorGlintConsumer(
                vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(textureId),
                false,
                itemStack.hasGlint()
            )
            elytraModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)
            matrices.pop()
        }
    }

    companion object {
        private val REINFORCED_ELYTRA_SKIN = identifier("textures/entity/reinforced_elytra.png")
    }
}
