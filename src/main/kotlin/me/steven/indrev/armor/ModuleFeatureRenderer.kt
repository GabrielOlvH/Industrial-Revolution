package me.steven.indrev.armor

import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.utils.identifier
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.util.Identifier

class ModuleFeatureRenderer<T : LivingEntity, M : BipedEntityModel<T>, A : BipedEntityModel<T>>(
    context: FeatureRendererContext<T, M>,
    private val leggingsModel: A,
    private val bodyModel: A
) : ArmorFeatureRenderer<T, M, A>(context, leggingsModel, bodyModel) {

    override fun render(matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, i: Int, livingEntity: T, f: Float, g: Float, h: Float, j: Float, k: Float, l: Float) {
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.CHEST, i, getArmor(EquipmentSlot.CHEST))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.LEGS, i, getArmor(EquipmentSlot.LEGS))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.FEET, i, getArmor(EquipmentSlot.FEET))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.HEAD, i, getArmor(EquipmentSlot.HEAD))
    }

    private fun renderArmor(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, livingEntity: T, equipmentSlot: EquipmentSlot, light: Int, bipedEntityModel: A) {
        val itemStack = livingEntity.getEquippedStack(equipmentSlot)
        val item = itemStack.item as? IRModularArmorItem ?: return
        if (item.slotType == equipmentSlot) {
            (this.contextModel as BipedEntityModel<T>).setAttributes(bipedEntityModel)
            setVisible(bipedEntityModel, equipmentSlot)
            val rgb = item.getColor(itemStack)
            val r = (rgb and 0xFF0000 shr 16) / 255f
            val g = (rgb and 0xFF00 shr 8) / 255f
            val b = (rgb and 0xFF) / 255f
            item.getInstalled(itemStack).filter { it.slots.contains(equipmentSlot) }.forEach { module ->
                if (module.hasTexture) {
                    renderArmorParts(
                        matrices, vertexConsumers, light, item, itemStack.hasGlint(), bipedEntityModel, usesSecondLayer(equipmentSlot), r, g, b, module.key
                    )
                    if (module.hasOverlay) {
                        renderArmorParts(
                            matrices, vertexConsumers, 15728880, item, itemStack.hasGlint(), bipedEntityModel, usesSecondLayer(equipmentSlot), r, g, b, "${module.key}_overlay"
                        )
                    }
                }
            }
        }
    }

    private fun renderArmorParts(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int,
        armorItem: ArmorItem,
        hasGlint: Boolean,
        bipedEntityModel: A,
        secondLayer: Boolean,
        r: Float, g: Float, b: Float,
        overlay: String?) {
        val vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(getArmorTexture(armorItem, secondLayer, overlay)), false, hasGlint)
        bipedEntityModel.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, r, g, b, 1.0f)
    }

    private fun getArmor(slot: EquipmentSlot): A {
        return if (usesSecondLayer(slot)) leggingsModel else bodyModel
    }

    private fun usesSecondLayer(slot: EquipmentSlot): Boolean {
        return slot == EquipmentSlot.LEGS
    }

    private fun getArmorTexture(armorItem: ArmorItem, secondLayer: Boolean, overlay: String?): Identifier {
        val path = "textures/models/armor/" + armorItem.material.name + "_layer_" + (if (secondLayer) 2 else 1) + (if (overlay == null) "" else "_$overlay") + ".png"
        return MODULAR_ARMOR_TEXTURE_CACHE.computeIfAbsent(path) { id -> identifier(id) }
    }

    companion object {
        val MODULAR_ARMOR_TEXTURE_CACHE = mutableMapOf<String, Identifier>()
    }
}