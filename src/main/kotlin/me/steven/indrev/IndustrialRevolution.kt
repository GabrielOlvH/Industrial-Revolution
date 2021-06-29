package me.steven.indrev

import dev.cafeteria.fakeplayerapi.server.FakePlayerBuilder
import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer
import io.github.ladysnake.pal.AbilitySource
import io.github.ladysnake.pal.Pal
import me.steven.indrev.api.IRServerPlayerEntityExtension
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.datagen.DataGeneratorManager
import me.steven.indrev.gui.screenhandlers.COAL_GENERATOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.items.armor.ReinforcedElytraItem
import me.steven.indrev.mixin.common.AccessorItemTags
import me.steven.indrev.networks.NetworkEvents
import me.steven.indrev.packets.PacketRegistry
import me.steven.indrev.packets.client.SyncConfigPacket
import me.steven.indrev.packets.client.SyncVeinTypesPacket
import me.steven.indrev.recipes.SelfRemainderRecipe
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.*
import me.steven.indrev.utils.getRecipes
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.chunkveins.VeinTypeResourceListener
import net.adriantodt.fallflyinglib.FallFlyingLib
import net.adriantodt.fallflyinglib.event.PreFallFlyingCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ElytraItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.tag.Tag
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
        
        arrayOf(
            IRFluidRegistry.COOLANT_STILL,
            IRFluidRegistry.MOLTEN_NETHERITE_STILL,
            IRFluidRegistry.MOLTEN_IRON_STILL,
            IRFluidRegistry.MOLTEN_GOLD_STILL,
            IRFluidRegistry.MOLTEN_COPPER_STILL,
            IRFluidRegistry.MOLTEN_TIN_STILL,
            IRFluidRegistry.MOLTEN_SILVER_STILL,
            IRFluidRegistry.MOLTEN_LEAD_STILL,
            IRFluidRegistry.SULFURIC_ACID_STILL,
            IRFluidRegistry.TOXIC_MUD_STILL,
            IRFluidRegistry.HYDROGEN_STILL,
            IRFluidRegistry.OXYGEN_STILL,
            IRFluidRegistry.METHANE_STILL,
        ).forEach { it.registerFluidKey() }

        IRLootTables.register()

        MachineRegistry

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, SmelterRecipe.IDENTIFIER, SmelterRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SmelterRecipe.IDENTIFIER, SmelterRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CondenserRecipe.IDENTIFIER, CondenserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CondenserRecipe.IDENTIFIER, CondenserRecipe.TYPE)
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

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(VeinTypeResourceListener())

        ServerTickEvents.END_WORLD_TICK.register(NetworkEvents)
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(NetworkEvents)

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            SyncVeinTypesPacket.sendVeinTypes(player)
            SyncConfigPacket.sendConfig(player)
            if (player is IRServerPlayerEntityExtension) {
                (player as IRServerPlayerEntityExtension).sync()
            }
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->

                if (player is IRServerPlayerEntityExtension && player.shouldSync()) {
                    player.sync()
                }

                val handler = player.currentScreenHandler as? IRGuiScreenHandler ?: return@forEach
                handler.ctx.run { world, pos ->
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                    blockEntity.sync()
                }
            }
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { s, _, _ ->
            s.recipeManager.getRecipes().keys.filterIsInstance<IRRecipeType<*>>().forEach { it.clearCache() }
        }

        if (FabricLoader.getInstance().getLaunchArguments(true).contains("-dataGen")) {
            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                DataGeneratorManager("indrev").generate()
            })
        }

        PreFallFlyingCallback.EVENT.register { player ->
            if (player.world.isClient) return@register
            val armorStack = player.inventory.getArmorStack(EquipmentSlot.CHEST.entitySlotId)
            val tracker = FallFlyingLib.ABILITY.getTracker(player)
            if (ReinforcedElytraItem.canFallFly(armorStack)) {
                tracker.addSource(REINFORCED_ELYTRA_SOURCE)
                val i = player.roll + 1
                if (i % 10 == 0 && (i / 10) % 2 == 0) {
                    armorStack.damage(1, player) { player -> player.sendEquipmentBreakStatus(EquipmentSlot.CHEST) }
                }
            } else {
                tracker.removeSource(REINFORCED_ELYTRA_SOURCE)
            }
        }

        FabricModelPredicateProviderRegistry.register(IRItemRegistry.REINFORCED_ELYTRA, identifier("broken")) { stack, _, _, _ ->
            if (ElytraItem.isUsable(stack)) 0.0f else 1.0f
        }

        LOGGER.info("Industrial Revolution has initialized.")
    }

    val LOGGER: Logger = LogManager.getLogger("Industrial Revolution")

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack { MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK4).asItem() } }

    val COOLERS_TAG: Tag.Identified<Item> = AccessorItemTags.getRequiredTagList().add("indrev:coolers")
    val WRENCH_TAG: Tag.Identified<Item> = AccessorItemTags.getRequiredTagList().add("c:wrenches")
    val SCREWDRIVER_TAG: Tag.Identified<Item> = AccessorItemTags.getRequiredTagList().add("c:screwdrivers")

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

    val REINFORCED_ELYTRA_SOURCE: AbilitySource = Pal.getAbilitySource(identifier("reinforced_elytra"))
}