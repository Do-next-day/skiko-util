package top.e404.skiko.draw.test

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.junit.Test
import top.e404.skiko.draw.render3d.*
import java.io.File
import kotlin.math.pow
import kotlin.random.Random

typealias ColorProvider = (value: Int, week: Int, day: Int) -> Int

class TestRender3d {

    @Test
    fun heatmap() {
        // 生成随机的热力图数据
        fun generateBarData(numWeeks: Int, numDaysPerWeek: Int, maxValue: Int) = (0 until numWeeks).map {
            (0 until numDaysPerWeek).map {
                if (Random.nextInt(3) > 1) Random.nextInt(
                    0,
                    maxValue
                ) else 0
            }
        }

        // 创建一个非线性的颜色提供器，颜色根据周（色相）和值（亮度）变化
        fun createNonLinearColorProvider(totalWeeks: Int, maxValue: Int): ColorProvider {
            return provider@{ value, week, _ ->
                val hue = 1 - week.toFloat() / totalWeeks
                if (value == 0) return@provider java.awt.Color.HSBtoRGB(hue, 0.5f, 0.3f) or (0xff shl 24)
                val normalizedValue = value.toFloat() / maxValue.toFloat()
                val brightnessCurve = normalizedValue.pow(2.0f)
                val brightness = 0.5f + brightnessCurve * 0.3f
                java.awt.Color.HSBtoRGB(hue, 0.7f, brightness) or (0xff shl 24)
            }
        }
        // 定义热力图参数
        val TOTAL_WEEKS = 54
        val DAYS_PER_WEEK = 7
        val MAX_VALUE = 6
        val barData = generateBarData(TOTAL_WEEKS, DAYS_PER_WEEK, MAX_VALUE)
        val colorProvider = createNonLinearColorProvider(TOTAL_WEEKS, MAX_VALUE)
        val barSize = 1.0f
        val barSpacing = .15f
        // 为每个数据点创建一个长方体Mesh
        val componentMeshes = barData.flatMapIndexed { wi, weekData ->
            weekData.mapIndexed { di, value ->
                val height = if (value == 0) 0.1f else value * 0.3f
                val color = colorProvider(value, wi, di)
                val cuboid = createCuboid(Vec3(barSize, height, barSize), color)
                // 将长方体移动到其在网格中的正确位置
                val x = wi * (barSize + barSpacing)
                val y = height / 2f
                val z = di * (barSize + barSpacing)
                Mesh(cuboid.vertices.map { Vertex(it.position + Vec3(x, y, z), it.uv) }, cuboid.faces)
            }
        }
        // 将所有小长方体合并成一个大Mesh
        val chartMesh = combineMeshes(componentMeshes)
        // 计算图表中心，用于设置相机目标
        val chartWidth = (TOTAL_WEEKS - 1) * (barSize + barSpacing)
        val chartDepth = (DAYS_PER_WEEK - 1) * (barSize + barSpacing)
        val chartCenter = Vec3(chartWidth / 2f, MAX_VALUE * 0.3f / 2f, chartDepth / 2f)
        // 设置相机
        val camera = OrbitCamera(target = chartCenter, yaw = 45f, pitch = 35f, distance = 50f)
        val (viewMatrix, cameraForward) = camera.createViewMatrix()
        val solidBg = Color.makeRGB(0, 0, 15)
        val finalWidth = 1600
        val finalHeight = 1200
        // 渲染并保存透视投影实体图
        renderSceneToImage(
            Scene(listOf(chartMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                true,
                true,
                solidBg,
                useBackFaceCulling = true
            )
        ).let { File("heatmap_perspective_solid.png").writeBytes(it.encodeToData()!!.bytes) }
        // 渲染并保存正交投影实体图
        renderSceneToImage(
            Scene(listOf(chartMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                true,
                false,
                solidBg,
                useBackFaceCulling = true
            )
        ).let { File("heatmap_orthographic_solid.png").writeBytes(it.encodeToData()!!.bytes) }
        println("--- 3D热力图渲染完成 ---\n")
    }

    /**
     * 将Minecraft模型所有复杂的、硬编码的数据（尺寸、位置、UV坐标）封装起来
     *
     * 实现了数据与逻辑的完全分离，极大地提高了代码的可读性、可维护性和可扩展性
     */
    enum class BodyPart {
        HEAD {
            override fun getDims(isSlim: Boolean) = Vec3(8f, 8f, 8f)
            override fun getPos(isSlim: Boolean) = Vec3(0f, 20f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(16f, 8f, 8f, 8f),
                FaceDirection.LEFT to Rect.makeXYWH(0f, 8f, 8f, 8f),
                FaceDirection.TOP to Rect.makeXYWH(8f, 0f, 8f, 8f),
                FaceDirection.BOTTOM to Rect.makeXYWH(16f, 0f, 8f, 8f),
                FaceDirection.FRONT to Rect.makeXYWH(8f, 8f, 8f, 8f),
                FaceDirection.BACK to Rect.makeXYWH(24f, 8f, 8f, 8f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(48f, 8f, 8f, 8f),
                FaceDirection.LEFT to Rect.makeXYWH(32f, 8f, 8f, 8f),
                FaceDirection.TOP to Rect.makeXYWH(40f, 0f, 8f, 8f),
                FaceDirection.BOTTOM to Rect.makeXYWH(48f, 0f, 8f, 8f),
                FaceDirection.FRONT to Rect.makeXYWH(40f, 8f, 8f, 8f),
                FaceDirection.BACK to Rect.makeXYWH(56f, 8f, 8f, 8f)
            )
        },
        BODY {
            override fun getDims(isSlim: Boolean) = Vec3(8f, 12f, 4f)
            override fun getPos(isSlim: Boolean) = Vec3(0f, 10f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(16f, 20f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(28f, 20f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(20f, 16f, 8f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(28f, 16f, 8f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(20f, 20f, 8f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(32f, 20f, 8f, 12f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(16f, 36f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(28f, 36f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(20f, 32f, 8f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(28f, 32f, 8f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(20f, 36f, 8f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(32f, 36f, 8f, 12f)
            )
        },
        RIGHT_ARM {
            override fun getDims(isSlim: Boolean) = if (isSlim) Vec3(3f, 12f, 4f) else Vec3(4f, 12f, 4f)
            override fun getPos(isSlim: Boolean) = if (isSlim) Vec3(-5.5f, 10f, 0f) else Vec3(-6f, 10f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = if (isSlim) mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(40f, 20f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(47f, 20f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(44f, 16f, 3f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(47f, 16f, 3f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(44f, 20f, 3f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(51f, 20f, 3f, 12f)
            ) else mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(40f, 20f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(48f, 20f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(44f, 16f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(48f, 16f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(44f, 20f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(52f, 20f, 4f, 12f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = if (isSlim) mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(40f, 36f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(47f, 36f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(44f, 32f, 3f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(47f, 32f, 3f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(44f, 36f, 3f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(51f, 36f, 3f, 12f)
            ) else mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(40f, 36f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(48f, 36f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(44f, 32f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(48f, 32f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(44f, 36f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(52f, 36f, 4f, 12f)
            )
        },
        LEFT_ARM {
            override fun getDims(isSlim: Boolean) = if (isSlim) Vec3(3f, 12f, 4f) else Vec3(4f, 12f, 4f)
            override fun getPos(isSlim: Boolean) = if (isSlim) Vec3(5.5f, 10f, 0f) else Vec3(6f, 10f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = if (isSlim) mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(39f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(32f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(36f, 48f, 3f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(39f, 48f, 3f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(36f, 52f, 3f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(43f, 52f, 3f, 12f)
            ) else mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(40f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(32f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(36f, 48f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(40f, 48f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(36f, 52f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(44f, 52f, 4f, 12f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = if (isSlim) mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(55f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(48f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(52f, 48f, 3f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(55f, 48f, 3f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(52f, 52f, 3f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(59f, 52f, 3f, 12f)
            ) else mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(56f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(48f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(52f, 48f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(56f, 48f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(52f, 52f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(60f, 52f, 4f, 12f)
            )
        },
        RIGHT_LEG {
            override fun getDims(isSlim: Boolean) = Vec3(4f, 12f, 4f)
            override fun getPos(isSlim: Boolean) = Vec3(-2f, -2f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(0f, 20f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(8f, 20f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(4f, 16f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(8f, 16f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(4f, 20f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(12f, 20f, 4f, 12f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(0f, 36f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(8f, 36f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(4f, 32f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(8f, 32f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(4f, 36f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(12f, 36f, 4f, 12f)
            )
        },
        LEFT_LEG {
            override fun getDims(isSlim: Boolean) = Vec3(4f, 12f, 4f)
            override fun getPos(isSlim: Boolean) = Vec3(2f, -2f, 0f)
            override fun getBaseUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(24f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(16f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(20f, 48f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(24f, 48f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(20f, 52f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(28f, 52f, 4f, 12f)
            )

            override fun getOverlayUVs(isSlim: Boolean) = mapOf(
                FaceDirection.RIGHT to Rect.makeXYWH(8f, 52f, 4f, 12f),
                FaceDirection.LEFT to Rect.makeXYWH(0f, 52f, 4f, 12f),
                FaceDirection.TOP to Rect.makeXYWH(4f, 48f, 4f, 4f),
                FaceDirection.BOTTOM to Rect.makeXYWH(8f, 48f, 4f, 4f),
                FaceDirection.FRONT to Rect.makeXYWH(4f, 52f, 4f, 12f),
                FaceDirection.BACK to Rect.makeXYWH(12f, 52f, 4f, 12f)
            )
        };

        // 抽象方法，强制每个身体部件都必须提供自己的尺寸、位置和UV数据
        abstract fun getDims(isSlim: Boolean): Vec3
        abstract fun getPos(isSlim: Boolean): Vec3
        abstract fun getBaseUVs(isSlim: Boolean): Map<FaceDirection, Rect>
        abstract fun getOverlayUVs(isSlim: Boolean): Map<FaceDirection, Rect>?
    }

    /**
     * 根据皮肤贴图和模型类型（标准/Slim）创建完整的Minecraft玩家模型。
     */
    fun createMinecraftPlayer(skin: Bitmap, isSlim: Boolean): Mesh {
        val texW = skin.width.toFloat()
        val texH = skin.height.toFloat()
        val componentMeshes = mutableListOf<Mesh>()
        // 外层皮肤的膨胀量
        val overlayAmount = 0.25f
        val headOverlayAmount = 0.5f // 头部外层膨胀量稍大

        // 遍历所有身体部件
        for (part in BodyPart.entries) {
            val dims = part.getDims(isSlim)
            val pos = part.getPos(isSlim)
            val baseUVs = part.getBaseUVs(isSlim)
            val overlayUVs = part.getOverlayUVs(isSlim)

            // 创建并添加基础层
            val baseCuboid = createUVCuboid(dims, baseUVs, texW, texH)
            componentMeshes.add(Mesh(baseCuboid.vertices.map { Vertex(it.position + pos, it.uv) }, baseCuboid.faces))

            // 如果有外层定义，则创建并添加外层
            overlayUVs?.let { uvs ->
                val overlaySize = if (part == BodyPart.HEAD) headOverlayAmount else overlayAmount
                // 外层的尺寸比基础层稍大
                val overlayCuboid = createUVCuboid(dims + Vec3(overlaySize, overlaySize, overlaySize), uvs, texW, texH)
                componentMeshes.add(
                    Mesh(
                        overlayCuboid.vertices.map { Vertex(it.position + pos, it.uv) },
                        overlayCuboid.faces
                    )
                )
            }
        }
        // 将所有部件的Mesh合并成一个，并附上皮肤纹理
        return combineMeshes(componentMeshes, skin)
    }

    /**
     * 运行Minecraft皮肤渲染示例的函数。
     */
    fun runMinecraftSkinExample(modelName: String, skinFileName: String, isSlim: Boolean) {
        val skinFile = File(skinFileName)
        if (!skinFile.exists()) {
            error("错误: $skinFileName 文件未找到。请将皮肤贴图放置在运行目录下。")
        }
        // 从文件加载皮肤贴图
        val skinBitmap = Bitmap.makeFromImage(Image.makeFromEncoded(skinFile.readBytes()))
        // 创建玩家模型
        val playerMesh = createMinecraftPlayer(skinBitmap, isSlim)
        // 设置相机
        val camera = OrbitCamera(
            target = Vec3(0f, 12f, 0f),
            yaw = 45f,
            pitch = 20f,
            distance = 60f
        )
        val (viewMatrix, cameraForward) = camera.createViewMatrix()
        val solidBg = Color.makeRGB(80, 100, 130)
        val wireframeBg = Color.makeRGB(20, 25, 30)
        val finalWidth = 800
        val finalHeight = 1200
        // 渲染四种不同模式的图像
        // 注意：对于皮肤模型，背面剔除通常是关闭的(useBackFaceCulling = false)，因为外层皮肤的内侧也可能需要被看到。
        renderSceneToImage(
            Scene(listOf(playerMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                true,
                true,
                solidBg,
                useBackFaceCulling = false
            )
        ).let { File("${modelName}_perspective_solid.png").writeBytes(it.encodeToData()!!.bytes) }
        renderSceneToImage(
            Scene(listOf(playerMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                false,
                true,
                wireframeBg,
                useBackFaceCulling = false
            )
        ).let { File("${modelName}_perspective_wireframe.png").writeBytes(it.encodeToData()!!.bytes) }
        renderSceneToImage(
            Scene(listOf(playerMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                true,
                false,
                solidBg,
                useBackFaceCulling = false
            )
        ).let { File("${modelName}_orthographic_solid.png").writeBytes(it.encodeToData()!!.bytes) }
        // 为了演示背面剔除的效果，额外生成一张开启剔除的线框图
        renderSceneToImage(
            Scene(listOf(playerMesh)),
            RenderConfig(
                finalWidth,
                finalHeight,
                camera,
                false,
                false,
                wireframeBg,
                useBackFaceCulling = true
            )
        ).let { File("${modelName}_orthographic_wireframe_culling_on.png").writeBytes(it.encodeToData()!!.bytes) }
    }

    @Test
    fun skin() {
        runMinecraftSkinExample("steve", "steve_skin.png", isSlim = false)
        runMinecraftSkinExample("alex", "alex_skin.png", isSlim = true)
    }
}