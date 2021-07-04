package me.steven.indrev.compat.dashloader.factories

import me.steven.indrev.blocks.models.pipes.CableModel
import me.steven.indrev.compat.dashloader.models.DashCableModel
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.MultipartModelSelector
import net.minecraft.state.StateManager
import net.quantumfusion.dashloader.DashRegistry
import net.quantumfusion.dashloader.api.model.ModelFactory
import net.quantumfusion.dashloader.model.DashModel
import org.apache.commons.lang3.tuple.Pair

class DashCableFactory : ModelFactory {
    override fun toDash(
        model: BakedModel,
        registry: DashRegistry,
        var1: Pair<MutableList<MultipartModelSelector>, StateManager<Block, BlockState>>?
    ): DashModel = DashCableModel(model as CableModel, registry)

    override fun getType(): Class<out BakedModel> = CableModel::class.java

    override fun getDashType(): Class<out DashModel> = DashCableModel::class.java
}