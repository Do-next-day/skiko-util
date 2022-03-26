package top.e404.skiko.generator

import top.e404.skiko.ExtraData

fun interface ImageGenerator {
    suspend fun generate(data: ExtraData?): ByteArray
}