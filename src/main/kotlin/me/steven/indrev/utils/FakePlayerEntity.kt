package me.steven.indrev.utils

import com.mojang.authlib.GameProfile
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerInteractionManager
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import java.util.*

class FakePlayerEntity(world: ServerWorld, pos: BlockPos) :
    ServerPlayerEntity(world.server, world, GameProfile(FAKE_PLAYER_UUID, "indrev_fake_player"), ServerPlayerInteractionManager(world)) {

    init {
        setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    override fun tick() {
    }

    override fun playSound(event: SoundEvent?, category: SoundCategory?, volume: Float, pitch: Float) {
    }

    override fun playSound(sound: SoundEvent?, volume: Float, pitch: Float) {
    }

    override fun canStartRiding(entity: Entity?): Boolean = false

    override fun isSpectator(): Boolean = false

    override fun isCreative(): Boolean = false

    companion object {
        private val FAKE_PLAYER_UUID = UUID.randomUUID()
    }
}