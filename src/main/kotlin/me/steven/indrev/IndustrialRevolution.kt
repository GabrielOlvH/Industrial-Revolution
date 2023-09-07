package me.steven.indrev

import me.steven.indrev.api.IRServerPlayerEntityExtension
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.datagen.DataGeneratorManager
import me.steven.indrev.events.common.IRLootTableCallback
import me.steven.indrev.gui.screenhandlers.COAL_GENERATOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.networks.NetworkEvents
import me.steven.indrev.packets.PacketRegistry
import me.steven.indrev.packets.client.SyncConfigPacket
import me.steven.indrev.recipes.SelfRemainderRecipe
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.*
import me.steven.indrev.utils.hide
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object IndustrialRevolution : ModInitializer {
    override fun onInitialize() {

        Registry.register(Registries.ITEM_GROUP, identifier("mod_group"), MOD_GROUP)

        //load the screenhandlers.kt class
        COAL_GENERATOR_HANDLER

        IRConfig
        IRItemRegistry.registerAll()
        IRBlockRegistry.registerAll()
        IRFluidRegistry.registerAll()

        Registry.register(Registries.SOUND_EVENT, LASER_SOUND_ID, LASER_SOUND_EVENT)
        Registry.register(Registries.PARTICLE_TYPE, identifier("laser_particle"), LASER_PARTICLE)

        WorldGeneration.init()
        WorldGeneration.addFeatures()

        LootTableEvents.MODIFY.register(IRLootTableCallback)

        MachineRegistry

        Registry.register(Registries.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, SmelterRecipe.IDENTIFIER, SmelterRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, SmelterRecipe.IDENTIFIER, SmelterRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, CondenserRecipe.IDENTIFIER, CondenserRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, CondenserRecipe.IDENTIFIER, CondenserRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, DistillerRecipe.IDENTIFIER, DistillerRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, DistillerRecipe.IDENTIFIER, DistillerRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, SawmillRecipe.IDENTIFIER, SawmillRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, SawmillRecipe.IDENTIFIER, SawmillRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, ModuleRecipe.IDENTIFIER, ModuleRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, ModuleRecipe.IDENTIFIER, ModuleRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, LaserRecipe.IDENTIFIER, LaserRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, LaserRecipe.IDENTIFIER, LaserRecipe.TYPE)
        Registry.register(Registries.RECIPE_SERIALIZER, ElectrolysisRecipe.IDENTIFIER, ElectrolysisRecipe.SERIALIZER)
        Registry.register(Registries.RECIPE_TYPE, ElectrolysisRecipe.IDENTIFIER, ElectrolysisRecipe.TYPE)

        Registry.register(Registries.RECIPE_SERIALIZER, SelfRemainderRecipe.IDENTIFIER, SelfRemainderRecipe.SERIALIZER)

        PacketRegistry.registerServer()

        ServerTickEvents.END_WORLD_TICK.register(NetworkEvents)
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(NetworkEvents)
        ServerLifecycleEvents.SERVER_STOPPING.register(NetworkEvents)

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            SyncConfigPacket.sendConfig(player)
            if (player is IRServerPlayerEntityExtension) {
                player.indrev_sync()
            }
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->
                if (player is IRServerPlayerEntityExtension && player.indrev_shouldSync()) {
                    player.indrev_sync()
                }
            }
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->
                val currentScreenHandler = player.currentScreenHandler as? IRGuiScreenHandler ?: return@forEach
                currentScreenHandler.syncProperties()
            }
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { s, _, _ ->
            s.recipeManager.recipes.keys.filterIsInstance<IRRecipeType<*>>().forEach { it.clearCache() }
        }

        LOGGER.info("Industrial Revolution has initialized.")
    }

    val LOGGER: Logger = LogManager.getLogger("Industrial Revolution")

    const val MOD_ID = "indrev"

    val MOD_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, identifier("mod_group"))

    val MOD_GROUP: ItemGroup =
        FabricItemGroup.builder().icon { ItemStack { MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK4).asItem() } }.displayName(Text.literal("indrev.indrev_group")).build()

    val COOLERS_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("coolers"))
    val WRENCH_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:wrenches"))
    val SCREWDRIVER_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:screwdrivers"))
    val NIKOLITE_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:nikolite_ores"))
    val TIN_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:tin_ores"))
    val LEAD_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:lead_ores"))
    val SILVER_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:silver_ores"))
    val TUNGSTEN_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:tungsten_ores"))
    val ANCIENT_DEBRIS_ORES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:ancient_debris_ores"))

    val LASER_SOUND_ID = identifier("laser")
    val LASER_SOUND_EVENT = SoundEvent.of(LASER_SOUND_ID)
    val LASER_PARTICLE = FabricParticleTypes.simple()
}