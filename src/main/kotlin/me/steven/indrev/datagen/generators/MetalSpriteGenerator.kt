package me.steven.indrev.datagen.generators

import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.ImageFactory
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage
import java.io.File

class MetalSpriteGenerator(
    val root: File,
    namespace: String,
    fallback: (Identifier) -> ImageFactory<Identifier>
) : DataGenerator<Identifier, BufferedImage>(File(root, "textures"), namespace, fallback) {
    override fun generate(): Int {
        var count = 0
        generators.forEach { (tag, _) ->
            if (generate(tag, tag))
                count++
        }
        return count
    }
}