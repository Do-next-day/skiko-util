package top.e404.skiko.lowpoly

import org.jetbrains.skia.*
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.withCanvas

object LowPoly {
    fun Image.lowpolyBySize(
        accuracy: Int = 100
    ) = lowpoly(
        accuracy,
        pointCount = width * height / 25
    )

    fun Image.lowpoly(
        accuracy: Int = 100,
        pointCount: Int = 300
    ) = generate(toBitmap(), accuracy, pointCount)

    /**
     * 生成low poly风格的图片
     *
     * @param bitmap 源图片
     * @param accuracy 精度值，越小精度越高
     * @param pointCount 随机点的数量
     */
    fun generate(
        bitmap: Bitmap,
        accuracy: Int,
        pointCount: Int
    ): Image {
        // 宽高
        val width = bitmap.width
        val height = bitmap.height
        val collectors = ArrayList<IntArray>()
        val particles = ArrayList<IntArray>()
        Sobel.sobel(bitmap) { magnitude: Int, x: Int, y: Int ->
            if (magnitude > 40) collectors.add(intArrayOf(x, y))
        }
        for (i in 0 until pointCount) {
            particles.add(intArrayOf((Math.random() * width).toInt(), (Math.random() * height).toInt()))
        }
        val len = collectors.size / accuracy
        for (i in 0 until len) {
            val random = (Math.random() * collectors.size).toInt()
            particles.add(collectors[random])
            collectors.removeAt(random)
        }
        particles.add(intArrayOf(0, 0))
        particles.add(intArrayOf(0, height))
        particles.add(intArrayOf(width, 0))
        particles.add(intArrayOf(width, height))
        val triangles = Delaunay.triangulate(particles)
        var x1: Float
        var x2: Float
        var x3: Float
        var y1: Float
        var y2: Float
        var y3: Float
        var cx: Float
        var cy: Float

        return Surface.makeRaster(bitmap.imageInfo).withCanvas {
            var i = 0
            val paint = Paint()
            while (i < triangles.size) {
                x1 = particles[triangles[i]][0].toFloat()
                x2 = particles[triangles[i + 1]][0].toFloat()
                x3 = particles[triangles[i + 2]][0].toFloat()
                y1 = particles[triangles[i]][1].toFloat()
                y2 = particles[triangles[i + 1]][1].toFloat()
                y3 = particles[triangles[i + 2]][1].toFloat()
                cx = (x1 + x2 + x3) / 3
                cy = (y1 + y2 + y3) / 3
                drawVertices(
                    VertexMode.TRIANGLES,
                    floatArrayOf(
                        x1, y1,
                        x2, y2,
                        x3, y3,
                    ),
                    blendMode = BlendMode.COLOR,
                    paint = paint.apply {
                        color = bitmap.getColor(cx.toInt(), cy.toInt())
                    }
                )
                i += 3
            }
        }
    }
}
