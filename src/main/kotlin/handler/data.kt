@file:Suppress("UNUSED")

package top.e404.skiko.handler

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData

data class IntData(var data: Int) : ExtraData
data class IntPairData(val i1: Int, val i2: Int) : ExtraData
data class LongData(val data: Long) : ExtraData
data class DoubleData(val data: Double) : ExtraData
data class FloatData(val data: Float) : ExtraData
data class StringData(val data: String) : ExtraData
data class StringPairData(val s1: String, val s2: String) : ExtraData

data class ImageData(val data: Image) : ExtraData
data class ImagePairData(val i1: Image, val i2: Image) : ExtraData

data class TextData(val text: String, val fontSize: Int?, val stroke: Int?) : ExtraData