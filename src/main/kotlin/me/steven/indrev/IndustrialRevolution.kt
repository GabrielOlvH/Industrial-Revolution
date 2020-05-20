package me.steven.indrev

import me.steven.indrev.blocks.GeneratorBlockEntity
import me.steven.indrev.gui.CoalGeneratorController
import me.steven.indrev.registry.GeneratorRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.container.ContainerFactory
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import team.reborn.energy.Energy
import team.reborn.energy.minecraft.EnergyModInitializer


class IndustrialRevolution : EnergyModInitializer() {
    override fun onInitialize() {
        super.onInitialize()
        Energy.registerHolder(GeneratorBlockEntity::class.java) { obj -> obj as GeneratorBlockEntity }
        GeneratorRegistry().registerAll()
        ContainerProviderRegistry.INSTANCE.registerFactory(COAL_GENERATOR_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorController(
                syncId,
                player.inventory,
                BlockContext.create(player.world, buf.readBlockPos())
            )
        }
    }

    companion object {
        const val MOD_ID = "indrev"

        val MOD_GROUP = FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack(GeneratorRegistry.COAL_GENERATOR_BLOCK_ITEM) }

        val COAL_GENERATOR_SCREEN_ID = identifier("coal_generator_screen")
    }
}