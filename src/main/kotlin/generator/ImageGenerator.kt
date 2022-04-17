package top.e404.skiko.generator

import top.e404.skiko.frame.Frame

fun interface ImageGenerator {
    suspend fun generate(args: MutableMap<String, String>): MutableList<Frame>
}