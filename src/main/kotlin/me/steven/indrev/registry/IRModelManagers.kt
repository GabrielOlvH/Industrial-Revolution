package me.steven.indrev.registry

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.DrillHeadModel
import me.steven.indrev.blocks.models.CableModel
import me.steven.indrev.blocks.models.PumpPipeBakedModel
import me.steven.indrev.items.models.TankItemBakedModel
import me.steven.indrev.utils.SimpleBlockModel
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.model.ExtraModelProvider
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.util.function.Consumer

object IRModelManagers : ModelVariantProvider, ExtraModelProvider {

    private val LFC_OVERLAY_REGEX = Regex("lazuli_flux_container_(input|output|item_lf_level|mk[1-4]_overlay)")
    private val CABLE_MODELS = arrayOf(
        CableModel(Tier.MK1), CableModel(Tier.MK2), CableModel(Tier.MK3), CableModel(Tier.MK4)
    )

    override fun loadModelVariant(resourceId: ModelIdentifier, ctx: ModelProviderContext?): UnbakedModel? {
        if (resourceId.namespace != "indrev") return null
        val path = resourceId.path
        val variant = resourceId.variant
        val id = Identifier(resourceId.namespace, resourceId.path)
        return when {
            path == "drill_head" -> DrillHeadModel(resourceId.variant)
            path == "pump_pipe" -> PumpPipeBakedModel()
            LFC_OVERLAY_REGEX.matches(path) -> SimpleBlockModel(path)
            path == "tank" && variant == "inventory" -> TankItemBakedModel
            path.startsWith("cable_mk") -> CABLE_MODELS[path.last().toString().toInt() - 1]
            MachineRegistry.MAP.containsKey(id) -> MachineRegistry.MAP[id]?.modelProvider?.get(Tier.values()[(path.last().toString().toIntOrNull() ?: 4) - 1])?.invoke(id.path.replace("_creative", "_mk4"))
            else -> return null
        }
    }

    override fun provideExtraModels(manager: ResourceManager?, out: Consumer<Identifier>) {
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
    }
}
