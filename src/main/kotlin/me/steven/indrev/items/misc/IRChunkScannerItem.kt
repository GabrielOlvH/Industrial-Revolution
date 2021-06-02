package me.steven.indrev.items.misc

import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.asString
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinState
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.biome.BuiltinBiomes
import kotlin.random.asKotlinRandom

class IRChunkScannerItem(settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        super.appendTooltip(stack, world, tooltip, context)
            tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip1").formatted(Formatting.BLUE, Formatting.ITALIC))
    }

    override fun finishUsing(stack: ItemStack, world: World?, user: LivingEntity?): ItemStack {
        if (world?.isClient == false) {
            val rnd = world.random.asKotlinRandom()
            val chunkPos = world.getChunk(user?.blockPos)?.pos
            if (chunkPos != null) {
                val state = ChunkVeinState.getState(world as ServerWorld)
                val isPresent = state.veins.containsKey(chunkPos)
                val info = state.veins[chunkPos]
                val default = BuiltinRegistries.BIOME.getKey(BuiltinBiomes.PLAINS).get()
                val biomeKey = world.registryManager.get(Registry.BIOME_KEY)
                    .getKey(world.getBiome(user?.blockPos))
                    .orElse(default)
                val picker = VeinType.BIOME_VEINS.getOrDefault(biomeKey, VeinType.BIOME_VEINS[default])
                //TODO why mojang aaaaaaaaaaaaaaaa
                val identifier = info?.veinIdentifier!! //picker?.pickRandom(world.random)!!
                val type = VeinType.REGISTERED[identifier]
                if (!isPresent) {
                    val data = ChunkVeinData(identifier, type!!.sizeRange.random(rnd))
                    state.veins[chunkPos] = data
                    state.markDirty()
                }
                val tag = NbtCompound()
                tag.putString("VeinIdentifier", identifier.toString())
                tag.putString("ChunkPos", chunkPos.asString())
                tag.putString("Dimension", world.registryKey.value.path)
                val infoStack = ItemStack(IRItemRegistry.SCAN_OUTPUT_ITEM)
                infoStack.tag = tag

                if (user is PlayerEntity) {
                    if (!user.inventory.insertStack(infoStack)) ItemScatterer.spawn(user.world, user.x, user.y, user.z, infoStack)
                    user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned1"), true)
                    user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned2", type), true)
                    world.playSound(user.x, user.y, user.z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f, false)
                }
                return stack
            }
        }
        return stack
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack?>? {
        val stack = user.getStackInHand(hand)
        if (world?.isClient == false) {
            user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanning"), true)
            user.setCurrentHand(hand)
        }
        return TypedActionResult.consume(stack)
    }

    override fun getMaxUseTime(stack: ItemStack?): Int = 100

    override fun getUseAction(stack: ItemStack?): UseAction = if (stack?.tag == null) UseAction.BOW else UseAction.NONE
}