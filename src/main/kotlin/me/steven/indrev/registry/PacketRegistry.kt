package me.steven.indrev.registry

import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.IRPlayerEntityExtension
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.GlobalStateController
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.MinerBlockEntity
import me.steven.indrev.blockentities.farms.RancherBlockEntity
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.controllers.machines.ModularWorkbenchController
import me.steven.indrev.gui.controllers.machines.RancherController
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.recipes.machines.ModuleRecipe
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.VeinType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry

object PacketRegistry {
    fun registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(Configurable.UPDATE_MACHINE_SIDE_PACKET_ID) { server, player, _, buf, _ ->
            val type = buf.readEnumConstant(ConfigurationType::class.java)
            val pos = buf.readBlockPos()
            val dir = Direction.byId(buf.readInt())
            val mode = TransferMode.values()[buf.readInt()]
            server.execute {
                val world = player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                blockEntity.getCurrentConfiguration(type)[dir] = mode
                blockEntity.markDirty()
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(Configurable.UPDATE_AUTO_OPERATION_PACKET_ID) { server, player, _, buf, _ ->
            val type = buf.readEnumConstant(ConfigurationType::class.java)
            val opType = buf.readByte()
            val pos = buf.readBlockPos()
            val value = buf.readBoolean()
            server.execute {
                val world = player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                if (opType.toInt() == 0)
                    blockEntity.getCurrentConfiguration(type).autoPush = value
                else
                    blockEntity.getCurrentConfiguration(type).autoPull = value
                blockEntity.markDirty()
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(AOEMachineBlockEntity.UPDATE_VALUE_PACKET_ID) { server, player, _, buf, _ ->
            val value = buf.readInt()
            val pos = buf.readBlockPos()
            val world = player.world
            server.execute {
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@execute
                    blockEntity.range = value
                    blockEntity.markDirty()
                    blockEntity.sync()
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(WFluid.FLUID_CLICK_PACKET) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val tank = buf.readInt()
            val world = player.world
            server.execute {
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                    val fluidComponent = blockEntity.fluidComponent ?: return@execute
                    FluidInvUtil.interactCursorWithTank(fluidComponent.getTank(tank), player, fluidComponent.getFilterForTank(tank))
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(IndustrialRevolution.UPDATE_MODULAR_TOOL_LEVEL) { server, player, _, buf, _ ->
            val key = buf.readString(32767)
            val value = buf.readInt()
            val slot = buf.readInt()
            server.execute {
                val stack = player.inventory.getStack(slot)
                val tag = stack.getOrCreateSubTag("selected")
                tag.putInt(key, value)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(SPLIT_STACKS_PACKET) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            server.execute {
                val world = player.world
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? CraftingMachineBlockEntity<*> ?: return@execute
                    blockEntity.isSplitOn = !blockEntity.isSplitOn
                    if (blockEntity.isSplitOn) blockEntity.splitStacks()
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RancherController.SYNC_RANCHER_CONFIG) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val feedBabies = buf.readBoolean()
            val mateAdults = buf.readBoolean()
            val matingLimit = buf.readInt()
            val killAfter = buf.readInt()
            server.execute {
                val world = player.world
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? RancherBlockEntity ?: return@execute
                    blockEntity.feedBabies = feedBabies
                    blockEntity.mateAdults = mateAdults
                    blockEntity.matingLimit = matingLimit
                    blockEntity.killAfter = killAfter
                    blockEntity.markDirty()
                    blockEntity.sync()
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(ModularWorkbenchController.MODULE_SELECT_PACKET) { server, player, _, buf, _ ->
            val screenHandler = player.currentScreenHandler as? ModularWorkbenchController ?: return@registerGlobalReceiver
            val recipeId = buf.readIdentifier()
            server.execute {
                val recipe = server.recipeManager.getAllOfType(ModuleRecipe.TYPE)[recipeId]!!
                screenHandler.layoutSlots(recipe)
                screenHandler.selected = recipe
            }
        }
    }

    fun syncVeinData(playerEntity: ServerPlayerEntity) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(VeinType.REGISTERED.size)
        VeinType.REGISTERED.forEach { (identifier, veinType) ->
            buf.writeIdentifier(identifier)
            val entries = veinType.outputs.entries
            buf.writeInt(entries.size)
            entries.forEach { entry ->
                val block = entry.element
                val weight = entry.weight
                val rawId = Registry.BLOCK.getRawId(block)
                buf.writeInt(rawId)
                buf.writeInt(weight)
            }
            buf.writeInt(veinType.sizeRange.first)
            buf.writeInt(veinType.sizeRange.last)
        }
        ServerPlayNetworking.send(playerEntity, IndustrialRevolution.SYNC_VEINS_PACKET, buf)
    }

    fun registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(IndustrialRevolution.SYNC_VEINS_PACKET) { _, _, buf, _ ->
            val totalVeins = buf.readInt()
            for (x in 0 until totalVeins) {
                val id = buf.readIdentifier()
                val entriesSize = buf.readInt()
                val outputs = WeightedList<Block>()
                for (y in 0 until entriesSize) {
                    val rawId = buf.readInt()
                    val weight = buf.readInt()
                    val block = Registry.BLOCK.get(rawId)
                    outputs.add(block, weight)
                }
                val minSize = buf.readInt()
                val maxSize = buf.readInt()
                val veinType = VeinType(id, outputs, minSize..maxSize)
                VeinType.REGISTERED[id] = veinType
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(IndustrialRevolution.SYNC_PROPERTY) { client, _, buf, _ ->
            val syncId = buf.readInt()
            val property = buf.readInt()
            val value = buf.readInt()
            client.execute {
                val handler = client.player!!.currentScreenHandler
                if (handler.syncId == syncId)
                    (handler as? IRGuiController)?.propertyDelegate?.set(property, value)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(MinerBlockEntity.BLOCK_BREAK_PACKET) { client, _, buf, _ ->
            val pos = buf.readBlockPos().down()
            val blockRawId = buf.readInt()
            val block = Registry.BLOCK.get(blockRawId)
            client.execute {
                MinecraftClient.getInstance().particleManager.addBlockBreakParticles(pos, block.defaultState)
                val blockSoundGroup = block.getSoundGroup(block.defaultState)
                (client.player!!.world as ClientWorld).playSound(
                    pos,
                    blockSoundGroup.breakSound,
                    SoundCategory.BLOCKS,
                    (blockSoundGroup.getVolume() + 1.0f) / 4.0f,
                    blockSoundGroup.getPitch() * 0.8f,
                    false
                )
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(IndustrialRevolution.SYNC_MODULE_PACKET) { client, _, buf, _ ->
            val size = buf.readInt()
            val modules = hashMapOf<ArmorModule, Int>()
            for (index in 0 until size) {
                val ordinal = buf.readInt()
                val module = ArmorModule.values()[ordinal]
                val level = buf.readInt()
                modules[module] = level
            }
            val durability = buf.readDouble()
            client.execute {
                val player = client.player!!
                if (player is IRPlayerEntityExtension) {
                    (player.getAppliedModules() as MutableMap<*, *>).clear()
                    modules.forEach(player::applyModule)
                    player.shieldDurability = durability
                }
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(IndustrialRevolution.RERENDER_CHUNK_PACKET) { client, _, buf, _ ->
            val pos = buf.readBlockPos()
            val world = client.player!!.world
            client.execute {
                val blockState = world.getBlockState(pos)
                MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, blockState, blockState, 8)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(IndustrialRevolution.SCHEDULE_RERENDER_CHUNK_PACKET) { _, _, buf, _ ->
            val time = buf.readInt()
            val pos = buf.readBlockPos()
            IndustrialRevolutionClient.positionsToRerender[pos] = time
        }

        ClientPlayNetworking.registerGlobalReceiver(GlobalStateController.UPDATE_PACKET_ID) { client, _, buf, _ ->
            val pos = buf.readBlockPos()
            val workingState = buf.readBoolean()
            val blockEntity = client.world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@registerGlobalReceiver
            blockEntity.workingState = workingState
            GlobalStateController.chunksToUpdate.add(ChunkPos(pos))

        }

    }
}