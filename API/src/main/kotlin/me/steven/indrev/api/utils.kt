package me.steven.indrev.api

import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

fun blockSpriteId(id: String) = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier("indrev-base", id))