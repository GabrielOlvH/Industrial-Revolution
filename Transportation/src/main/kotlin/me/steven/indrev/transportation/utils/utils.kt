package me.steven.indrev.transportation.utils

import me.steven.indrev.transportation.MOD_ID
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

internal fun identifier(path: String) = Identifier(MOD_ID, path)

fun blockSpriteId(id: String) = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier(id))

inline fun <T> transaction(block: (Transaction) -> T) = Transaction.openOuter().use(block)

inline fun <T> Transaction.nested(block: (Transaction) -> T) = this.openNested().use(block)