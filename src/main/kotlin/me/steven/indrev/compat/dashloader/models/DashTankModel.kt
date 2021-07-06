package me.steven.indrev.compat.dashloader.models

import me.steven.indrev.items.models.TankItemBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashConstructor
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.api.enums.ConstructorMode
import net.oskarstrom.dashloader.model.DashModel


@DashObject(TankItemBakedModel::class) class DashTankModel @DashConstructor(ConstructorMode.EMPTY) constructor() : DashModel {


    override fun toUndash(registry: DashRegistry): BakedModel {
        return TankItemBakedModel()
    }

    override fun getStage(): Int = 3
}