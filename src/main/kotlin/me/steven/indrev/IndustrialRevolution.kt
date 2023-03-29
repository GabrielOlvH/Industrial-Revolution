package me.steven.indrev

import dev.cafeteria.fakeplayerapi.server.FakePlayerBuilder
import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer
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
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object IndustrialRevolution : ModInitializer {
    override fun onInitialize() {

        //load the screenhandlers.kt class
        COAL_GENERATOR_HANDLER

        IRConfig
        IRItemRegistry.registerAll()
        IRBlockRegistry.registerAll()
        IRFluidRegistry.registerAll()

        Registry.register(Registry.SOUND_EVENT, LASER_SOUND_ID, LASER_SOUND_EVENT)
        Registry.register(Registry.PARTICLE_TYPE, identifier("laser_particle"), LASER_PARTICLE)

        WorldGeneration.init()
        WorldGeneration.addFeatures()

        LootTableEvents.MODIFY.register(IRLootTableCallback)

        MachineRegistry

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, SmelterRecipe.IDENTIFIER, SmelterRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SmelterRecipe.IDENTIFIER, SmelterRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CondenserRecipe.IDENTIFIER, CondenserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CondenserRecipe.IDENTIFIER, CondenserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, DistillerRecipe.IDENTIFIER, DistillerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, DistillerRecipe.IDENTIFIER, DistillerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, SawmillRecipe.IDENTIFIER, SawmillRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SawmillRecipe.IDENTIFIER, SawmillRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, ModuleRecipe.IDENTIFIER, ModuleRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, ModuleRecipe.IDENTIFIER, ModuleRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, LaserRecipe.IDENTIFIER, LaserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, LaserRecipe.IDENTIFIER, LaserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, ElectrolysisRecipe.IDENTIFIER, ElectrolysisRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, ElectrolysisRecipe.IDENTIFIER, ElectrolysisRecipe.TYPE)

        Registry.register(Registry.RECIPE_SERIALIZER, SelfRemainderRecipe.IDENTIFIER, SelfRemainderRecipe.SERIALIZER)

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

        if (FabricLoader.getInstance().getLaunchArguments(true).contains("-dataGen")) {
            FabricDataGenHelper.run()
            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                DataGeneratorManager("indrev").generate()
            })
        }
        LOGGER.info("Industrial Revolution has initialized.")
    }

    val LOGGER: Logger = LogManager.getLogger("Industrial Revolution")

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack { MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK4).asItem() } }

    val COOLERS_TAG: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, identifier("coolers"))
    val WRENCH_TAG: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:wrenches"))
    val SCREWDRIVER_TAG: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:screwdrivers"))
    val NIKOLITE_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:nikolite_ores"))
    val TIN_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:tin_ores"))
    val LEAD_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:lead_ores"))
    val SILVER_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:silver_ores"))
    val TUNGSTEN_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:tungsten_ores"))
    val ANCIENT_DEBRIS_ORES: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("c:ancient_debris_ores"))

    val LASER_SOUND_ID = identifier("laser")
    val LASER_SOUND_EVENT = SoundEvent(LASER_SOUND_ID)
    val LASER_PARTICLE = FabricParticleTypes.simple()

    val FAKE_PLAYER_BUILDER = FakePlayerBuilder(identifier("default_fake_player")) { builder, server, world, profile ->
        object : FakeServerPlayer(builder, server, world, profile) {
            override fun isCreative(): Boolean = false
            override fun isSpectator(): Boolean = false
            override fun playSound(sound: SoundEvent?, volume: Float, pitch: Float) {}
            override fun playSound(event: SoundEvent?, category: SoundCategory?, volume: Float, pitch: Float) {}
        }
    }
}