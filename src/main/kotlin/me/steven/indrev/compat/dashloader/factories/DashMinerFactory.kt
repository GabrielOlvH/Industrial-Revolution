package me.steven.indrev.compat.dashloader.factories

import me.steven.indrev.blocks.models.MinerBakedModel
import me.steven.indrev.compat.dashloader.models.DashMinerModel
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.MultipartModelSelector
import net.minecraft.state.StateManager
import net.quantumfusion.dashloader.DashRegistry
import net.quantumfusion.dashloader.api.model.ModelFactory
import net.quantumfusion.dashloader.model.DashModel
import org.apache.commons.lang3.tuple.Pair

class DashMinerFactory: ModelFactory {
    override fun toDash(
        model: BakedModel,
        registry: DashRegistry,
        var1: Pair<MutableList<MultipartModelSelector>, StateManager<Block, BlockState>>?
    ): DashModel = DashMinerModel(model as MinerBakedModel, registry)

    override fun getType(): Class<out BakedModel> = MinerBakedModel::class.java

    override fun getDashType(): Class<out DashModel> = DashMinerModel::class.java
}