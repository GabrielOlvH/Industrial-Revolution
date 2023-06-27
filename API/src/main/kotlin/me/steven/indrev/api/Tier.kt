package me.steven.indrev.api

enum class Tier(val asString: String, val transferCapacity: Long) {
    MK1("mk1", 32),
    MK2("mk2", 128),
    MK3("mk3", 512),
    MK4("mk4", 2048),
    CREATIVE("creative", Long.MAX_VALUE);

    fun getOverlaySprite() = blockSpriteId("block/${asString}_overlay")
}