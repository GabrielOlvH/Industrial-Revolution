package me.steven.indrev.events.client

import me.steven.indrev.armor.ModuleFeatureRenderer
import me.steven.indrev.armor.ReinforcedElytraFeatureRenderer
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity

object IRLivingEntityFeatureRendererCallback : LivingEntityFeatureRendererRegistrationCallback {
    override fun registerRenderers(
        entityType: EntityType<out LivingEntity>,
        renderer: LivingEntityRenderer<*, *>,
        helper: LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper,
        ctx: EntityRendererFactory.Context
    ) {
        val slim = false
        helper.register(
            ModuleFeatureRenderer(
                renderer as LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>>,
                BipedEntityModel(ctx.getPart(if (slim) EntityModelLayers.PLAYER_SLIM_INNER_ARMOR else EntityModelLayers.PLAYER_INNER_ARMOR)),
                BipedEntityModel(ctx.getPart(if (slim) EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR else EntityModelLayers.PLAYER_OUTER_ARMOR))
            )
        )

        helper.register(ReinforcedElytraFeatureRenderer(renderer, ctx.modelLoader))
    }
}