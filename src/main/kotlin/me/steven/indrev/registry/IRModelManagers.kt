package me.steven.indrev.registry

import me.steven.indrev.blocks.PumpPipeBakedModel
import me.steven.indrev.blocks.containers.LazuliFluxContainerBakedModel
import me.steven.indrev.blocks.machine.DrillHeadModel
import me.steven.indrev.utils.SimpleBlockModel
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.model.ModelAppender
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.resource.ResourceManager
import java.util.function.Consumer

object IRModelManagers : ModelVariantProvider, ModelAppender {

    private val LFC_OVERLAY_REGEX = Regex("lazuli_flux_container_(input|output|item_lf_level|mk[1-4]_overlay)")

    override fun loadModelVariant(resourceId: ModelIdentifier, ctx: ModelProviderContext?): UnbakedModel? {
        if (resourceId.namespace != "indrev") return null
        val path = resourceId.path
        return when {
            path == "drill_head" -> DrillHeadModel(resourceId.variant)
            path == "pump_pipe" -> PumpPipeBakedModel()
            LFC_OVERLAY_REGEX.matches(path) -> SimpleBlockModel(path)
            path.startsWith("lazuli_flux_container") -> LazuliFluxContainerBakedModel(path.replace("creative", "mk4"))
            MaterialHelper.MATERIAL_PROVIDERS.containsKey(resourceId) -> MaterialHelper.MATERIAL_PROVIDERS[resourceId]
            else -> return null
        }
    }

    override fun appendAll(manager: ResourceManager?, out: Consumer<ModelIdentifier>) {
        out.accept(ModelIdentifier(identifier("drill_head"), "stone"))
        out.accept(ModelIdentifier(identifier("drill_head"), "iron"))
        out.accept(ModelIdentifier(identifier("drill_head"), "diamond"))
        out.accept(ModelIdentifier(identifier("drill_head"), "netherite"))
        out.accept(ModelIdentifier(identifier("pump_pipe"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_input"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_output"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_item_lf_level"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_mk1_overlay"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_mk2_overlay"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_mk3_overlay"), ""))
        out.accept(ModelIdentifier(identifier("lazuli_flux_container_mk4_overlay"), ""))

        out.accept(ModelIdentifier(identifier("ingot_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("ingot_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("ingot_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("pickaxe_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("pickaxe_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("pickaxe_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("axe_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("axe_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("axe_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("helmet_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("helmet_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("helmet_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("dust_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("dust_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("dust_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("chestplate_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("chestplate_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("chestplate_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("boots_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("boots_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("boots_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("hoe_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("hoe_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("hoe_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("leggings_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("leggings_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("leggings_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("nugget_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("nugget_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("nugget_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("ore_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("ore_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("block_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("block_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("block_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("purified_ore_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("purified_ore_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("purified_ore_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("chunk_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("chunk_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("chunk_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("shovel_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("shovel_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("shovel_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("sword_base"), "inventory"))
        out.accept(ModelIdentifier(identifier("sword_shadow"), "inventory"))
        out.accept(ModelIdentifier(identifier("sword_highlight"), "inventory"))

        out.accept(ModelIdentifier(identifier("tool_stick"), "inventory"))
    }
}
