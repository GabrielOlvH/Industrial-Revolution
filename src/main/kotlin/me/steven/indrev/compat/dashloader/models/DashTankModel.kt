package me.steven.indrev.compat.dashloader.models

import me.steven.indrev.items.models.TankItemBakedModel
import net.minecraft.client.render.model.BakedModel
import net.quantumfusion.dashloader.DashRegistry
import net.quantumfusion.dashloader.model.DashModel

class DashTankModel : DashModel {

    override fun toUndash(registry: DashRegistry): BakedModel {
        return TankItemBakedModel()
    }

    override fun getStage(): Int = 3
}