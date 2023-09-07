package me.steven.indrev.api

enum class Tier(val asString: String, val transferCapacity: Long, val color: Int) {
    MK1("mk1", 32, 0xF48F73),
    MK2("mk2", 128, 0x00806D),
    MK3("mk3", 512, 0x006998),
    MK4("mk4", 2048, 0x5F707C),
    CREATIVE("creative", Long.MAX_VALUE, -1);

    fun getOverlaySprite() = blockSpriteId("block/${asString}_overlay")
}