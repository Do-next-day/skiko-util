package top.e404.skiko.draw.render3d

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color

/**
 * 创建一个平面的 Mesh (Quad)，支持细分以解决大平面渲染失真问题
 * @param center 中心点位置
 * @param size 尺寸 (宽, 高)
 * @param color 纯色
 * @param texture 可选纹理
 * @param normalDirection 法线朝向 (默认朝上 (0,1,0))
 */
fun createPlane(
    center: Vec3,
    size: Vec2,
    color: Int = Color.WHITE,
    texture: Bitmap? = null,
    normalDirection: Vec3 = Vec3(0f, 1f, 0f), // 默认地面
    segments: Int = 16 // 新增：细分段数
): Mesh {
    val halfW = size.u / 2f
    val halfH = size.v / 2f

    // 1. 构建切线空间 (TBN)
    val up = Vec3(0f, 1f, 0f)
    // 防止法线与 Up 向量平行导致叉乘为0
    val tangent = if (Math.abs(normalDirection.dot(up)) > 0.9f)
        Vec3(1f, 0f, 0f)
    else
        normalDirection.cross(up).normalized()
    val bitangent = normalDirection.cross(tangent).normalized()

    val vertices = mutableListOf<Vertex>()
    val faces = mutableListOf<Face>()

    // 2. 生成网格顶点
    // 我们从左下角开始生成
    val startPos = center - tangent * halfW - bitangent * halfH

    // 步长
    val stepX = size.u / segments
    val stepY = size.v / segments
    val stepU = 1f / segments
    val stepV = 1f / segments

    for (y in 0..segments) {
        for (x in 0..segments) {
            // 计算当前顶点的世界坐标
            // Pos = Start + Tangent * (x * step) + Bitangent * (y * step)
            val pos = startPos + (tangent * (x * stepX)) + (bitangent * (y * stepY))

            // 计算 UV (0..1)
            // 注意：通常纹理坐标原点在左上或左下，这里假设 V 轴向上
            val uv = Vec2(x * stepU, 1f - y * stepV)

            vertices.add(Vertex(pos, uv))
        }
    }

    // 3. 生成网格面 (索引)
    val verticesPerRow = segments + 1
    for (y in 0 until segments) {
        for (x in 0 until segments) {
            // 计算当前格子的四个顶点索引
            val i0 = y * verticesPerRow + x
            val i1 = y * verticesPerRow + (x + 1)
            val i2 = (y + 1) * verticesPerRow + (x + 1)
            val i3 = (y + 1) * verticesPerRow + x

            // 添加两个三角形组成一个矩形格子
            // 注意顶点顺序以保证法线方向正确 (逆时针或顺时针)
            // 这里使用 0-1-2 和 0-2-3
            faces.add(Face(listOf(i0, i1, i2), color))
            faces.add(Face(listOf(i0, i2, i3), color))
        }
    }

    return Mesh(vertices, faces, texture)
}