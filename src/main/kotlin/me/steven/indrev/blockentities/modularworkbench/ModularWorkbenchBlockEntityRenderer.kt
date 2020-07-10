package me.steven.indrev.blockentities.modularworkbench

import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.ModularArmorFeatureRenderer
import me.steven.indrev.armor.Module
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.utils.identifier
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class ModularWorkbenchBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) :
    BlockEntityRenderer<ModularWorkbenchBlockEntity>(dispatcher) {
    val bodyModel = BipedEntityModel<AbstractClientPlayerEntity>(0.5f)
    val leggingsModel = BipedEntityModel<AbstractClientPlayerEntity>(1.0f)

    init {
        bodyModel.setVisible(false)
        leggingsModel.setVisible(false)
    }

    override fun render(
        entity: ModularWorkbenchBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val armor = entity?.inventoryController?.inventory?.getStack(2)
        if (armor?.isEmpty == false) {
            matrices.push()
            val yOffset = when ((armor.item as IRModularArmor).slotType) {
                EquipmentSlot.HEAD -> 1.0
                EquipmentSlot.CHEST -> 1.5
                EquipmentSlot.LEGS -> 1.7
                EquipmentSlot.FEET -> 2.0
                else -> -1.0
            }
            matrices.translate(0.5, yOffset, 0.5)
            matrices.multiply(
                Vector3f.POSITIVE_Y.getDegreesQuaternion(
                    270.0f * entity.animationProgress
                )
            )
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180f))
            renderArmor(matrices, vertexConsumers, armor, entity.world!!.getLightLevel(entity.pos.up(2)))
            matrices.pop()
        }
    }

    private fun renderArmor(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        itemStack: ItemStack,
        i: Int
    ) {
        val item = itemStack.item
        if (item is IRModularArmor && item.material == IRArmorMaterial.MODULAR) {
            val slotType = item.slotType
            val bipedEntityModel = getArmor(slotType)
            setVisible(slotType)
            val bl = usesSecondLayer(slotType)
            val bl2 = itemStack.hasGlint()
            // base will be done by the default armor renderer
            // renderArmorParts(matrices, vertexConsumers, i, item, bl2, bipedEntityModel, bl, 1.0f, 1.0f, 1.0f, null)
            val rgb = item.getColor(itemStack)
            val r = (rgb and 0xFF0000 shr 16) / 255f
            val g = (rgb and 0xFF00 shr 8) / 255f
            val b = (rgb and 0xFF) / 255f
            renderArmorParts(
                matrices, vertexConsumers, 15728640, item, bl2, bipedEntityModel, bl, r, g, b, null
            )
            Module.getInstalled(itemStack).filter { it.slots.contains(slotType) }.forEach { module ->
                if (module != Module.COLOR) {
                    renderArmorParts(
                        matrices, vertexConsumers, 15728640, item, bl2, bipedEntityModel, bl, r, g, b, module.key
                    )
                }
            }
        }
    }

    private fun renderArmorParts(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        armorItem: ArmorItem,
        bl: Boolean,
        bipedEntityModel: BipedEntityModel<AbstractClientPlayerEntity>,
        bl2: Boolean,
        r: Float,
        g: Float,
        b: Float,
        string: String?
    ) {
        val vertexConsumer = ItemRenderer.method_27952(
            vertexConsumerProvider,
            RenderLayer.getArmorCutoutNoCull(getArmorTexture(armorItem, bl2, string)),
            false,
            bl
        )
        bipedEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, r, g, b, 1.0f)
    }

    private fun getArmor(slot: EquipmentSlot): BipedEntityModel<AbstractClientPlayerEntity> {
        return if (usesSecondLayer(slot)) leggingsModel else bodyModel
    }

    private fun usesSecondLayer(slot: EquipmentSlot): Boolean {
        return slot == EquipmentSlot.LEGS
    }

    fun setVisible(slot: EquipmentSlot?) {
        bodyModel.head.visible = slot == EquipmentSlot.HEAD
        bodyModel.helmet.visible = slot == EquipmentSlot.HEAD

        bodyModel.torso.visible = slot == EquipmentSlot.CHEST
        bodyModel.rightArm.visible = slot == EquipmentSlot.CHEST
        bodyModel.leftArm.visible = slot == EquipmentSlot.CHEST

        leggingsModel.torso.visible = slot == EquipmentSlot.LEGS
        leggingsModel.rightLeg.visible = slot == EquipmentSlot.LEGS
        leggingsModel.leftLeg.visible = slot == EquipmentSlot.LEGS

        bodyModel.rightLeg.visible = slot == EquipmentSlot.FEET
        bodyModel.leftLeg.visible = slot == EquipmentSlot.FEET


    }

    private fun getArmorTexture(armorItem: ArmorItem, bl: Boolean, string: String?): Identifier? {
        val string2 =
            "textures/models/armor/" + armorItem.material.name + "_layer_" + (if (bl) 2 else 1) + (if (string == null) "" else "_$string") + ".png"
        return ModularArmorFeatureRenderer.MODULAR_ARMOR_TEXTURE_CACHE.computeIfAbsent(string2) { id ->
            if (string == null) Identifier(id) else identifier(id)
        }
    }
}