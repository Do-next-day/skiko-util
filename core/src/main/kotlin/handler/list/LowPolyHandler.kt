package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.lowpoly.LowPoly.lowpoly

@ImageHandler
object LowPolyHandler : FramesHandler {
    override val name = "晶格化"
    override val regex = Regex("晶格化(?i)|lowPoly")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val accuracy = args["acc"]?.toIntOrNull()?.coerceIn(1..1000) ?: 100
        val pointCount = args["pc"]?.toIntOrNull()?.coerceIn(1..1000) ?: 200
        return frames.result {
            common(args).handle {
                it.lowpoly(accuracy, pointCount)
            }
        }
    }
}

