package me.steven.indrev.blockentities.cables

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.Container
import net.minecraft.container.PropertyDelegate
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class CableBlockEntity(tier: Tier) :
    MachineBlockEntity(tier, MachineRegistry.CABLE_REGISTRY) {
    var cover: Identifier? = null

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun createContainer(i: Int, playerInventory: PlayerInventory): Container = throw IllegalArgumentException("cables do not have containers!")

    override fun getContainerName(): Text = throw IllegalArgumentException("cables do not have containers!")

    override fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover = Identifier(tag.getString("cover"))
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover = Identifier(tag.getString("cover"))
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toClientTag(tag)
    }
}