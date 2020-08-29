package me.steven.indrev.utils

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class FakePlayerEntity(world: World, pos: BlockPos) :
    PlayerEntity(world, pos, 1f, GameProfile(FAKE_PLAYER_UUID, "indrev_fake_player")) {

    init {
        setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }


    override fun playSound(event: SoundEvent?, category: SoundCategory?, volume: Float, pitch: Float) {
    }

    override fun playSound(sound: SoundEvent?, volume: Float, pitch: Float) {
    }

    override fun isSpectator(): Boolean = true

    override fun isCreative(): Boolean = true

    companion object {
        private val FAKE_PLAYER_UUID = UUID.randomUUID()
    }
}