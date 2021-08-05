package me.steven.indrev.packets.client

import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.registry.Registry

object MiningRigSpawnBlockParticlesPacket {

    val BLOCK_BREAK_PACKET = identifier("miner_drill_block_particle") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(BLOCK_BREAK_PACKET) { client, _, buf, _ ->
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
    }
}