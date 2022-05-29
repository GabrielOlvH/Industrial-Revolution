package me.steven.indrev.datagen

import me.steven.indrev.datagen.utils.MetalModel
import me.steven.indrev.datagen.utils.MetalSpriteRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

interface ImageFactory<T> : DataFactory<T, BufferedImage> {

    override val extension: String get() = "png"

    override fun write(file: File, t: BufferedImage) {
        ImageIO.write(t, "png", file)
    }

    companion object {

        fun <T> simpleFactory(): (ModelIdentifier) -> ImageFactory<T> = { string ->
            object : ImageFactory<T> {
                override fun generate(): BufferedImage {
                    val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                    val baseGraphics = img.createGraphics()
                    val model = MetalSpriteRegistry.MATERIAL_PROVIDERS[string]
                    (model as MetalModel).holders.forEach { modelWithColor ->
                        val type = if (model.type == MetalModel.TransformationType.BLOCK) "block" else "item"
                        val resourceId = Identifier(modelWithColor.id.namespace, "textures/$type/${modelWithColor.id.path}.png")
                        val inputStream = MinecraftClient.getInstance().resourceManager.getResource(resourceId).get().inputStream
                        val overlay = ImageIO.read(inputStream)
                        for (x in 0 until 16) {
                            for (y in 0 until 16) {
                                val overlayHex = overlay.getRGB(x, y)
                                val a = ((overlayHex shr 24) and 255).toFloat() / 255f
                                val r = ((overlayHex shr 16) and 255).toFloat() / 255f
                                val g = ((overlayHex shr 8) and 255).toFloat() / 255f
                                val b = ((overlayHex shr 0) and 255).toFloat() / 255f

                                val hex = modelWithColor.color
                                val oA = ((hex shr 24) and 255).toFloat() / 255f
                                val oR = ((hex shr 16) and 255).toFloat() / 255f
                                val oG = ((hex shr 8) and 255).toFloat() / 255f
                                val oB = ((hex shr 0) and 255).toFloat() / 255f

                                val nR = mix(r, r * oR, oA)
                                val nG = mix(g, g * oG, oA)
                                val nB = mix(b, b * oB, oA)

                                val nHex = toHex(a, nR, nG, nB)

                                overlay.setRGB(x, y, nHex)
                            }
                        }
                        baseGraphics.drawImage(overlay, 0, 0, null)
                    }
                    baseGraphics.dispose()
                    return img
                }
            }
        }

        fun <T> simpleFactory0(): (ModelIdentifier) -> ImageFactory<T> = { string ->
            object : ImageFactory<T> {
                override fun generate(): BufferedImage {
                    val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                    val baseGraphics = img.createGraphics()
                    val model = MetalSpriteRegistry.MATERIAL_PROVIDERS[string]
                    (model as MetalModel).holders.forEach { modelWithColor ->
                        val type = if (model.type == MetalModel.TransformationType.BLOCK) "block" else "item"
                        val resourceId = Identifier(modelWithColor.id.namespace, "textures/$type/${modelWithColor.id.path}.png")
                        val inputStream = MinecraftClient.getInstance().resourceManager.getResource(resourceId).get().inputStream
                        val overlay = ImageIO.read(inputStream)
                        for (x in 0 until 16) {
                            for (y in 0 until 16) {
                                val overlayHex = overlay.getRGB(x, y)
                                val a = ((overlayHex shr 24) and 255).toFloat() / 255f
                                val r = ((overlayHex shr 16) and 255).toFloat() / 255f
                                val g = ((overlayHex shr 8) and 255).toFloat() / 255f
                                val b = ((overlayHex shr 0) and 255).toFloat() / 255f

                                val hex = modelWithColor.color
                                val oA = ((hex shr 24) and 255).toFloat() / 255f
                                val oR = ((hex shr 16) and 255).toFloat() / 255f
                                val oG = ((hex shr 8) and 255).toFloat() / 255f
                                val oB = ((hex shr 0) and 255).toFloat() / 255f

                                val nR = mix(r, r * oR, oA)
                                val nG = mix(g, g * oG, oA)
                                val nB = mix(b, b * oB, oA)

                                val nHex = toHex(a, nR, nG, nB)

                                overlay.setRGB(x, y, nHex)
                            }
                        }
                        if (modelWithColor.id.path == "tool_stick")
                            baseGraphics.drawImage(overlay, 0, -1, null)
                        else
                            baseGraphics.drawImage(overlay, 0, 0, null)
                    }
                    baseGraphics.dispose()
                    return img
                }
            }
        }

        private fun mix(x: Float, y: Float, a: Float): Float = x + (y - x) * a

        private fun toHex(a: Float, r: Float, g: Float, b: Float): Int =
            (a * 255).toInt() and 0xFF shl 24 or
                    ((r * 255).toInt() and 0xFF shl 16) or
                    ((g * 255).toInt() shl 8) or
                    ((b * 255).toInt() shl 0)

        fun <T> nullFactory(): ImageFactory<T> = object : ImageFactory<T> {
            override fun generate(): BufferedImage? = null
        }
    }
}