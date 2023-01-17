package com.example.arcorestudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30.*
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import com.example.arcorestudy.tools.VBOData

class CubeRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val texture1: Texture,
) : Scene() {
    val cubeVertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    )
    private lateinit var program: Program
    private var cube: VBOData =
        VBOData(cubeVertices, GL_STATIC_DRAW, 5)
    private var width: Int = 0
    private var height: Int = 0
    var proj = Mat4()
    var view = Mat4()
    var model = Mat4()
    var cubePositions = mutableListOf<Vec3>()
    var vboId = -1

    private fun getRandomPosition() {
        val random = java.util.Random()
        val list = mutableListOf<Vec3>()
        repeat(40) {
            val x = random.nextFloat() * 2 - 1
            val z = random.nextFloat() * 2 - 1
            val y = random.nextFloat() * 2 - 1
            list.add(Vec3(x, y, z))
        }
        cubePositions = list
    }

    init {
        //getRandomPosition()
    }

    override fun init(width: Int, height: Int) {
        glEnable(GL_DEPTH_TEST)
        this.width = width
        this.height = height
        texture1.load()
        program = Program.create(
            vertexShaderCode = vertexShaderCode,
            fragmentShaderCode = fragmentShaderCode
        )
        cube.bind()
        cube.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cube.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
    }

    override fun draw() {
        cube.draw()
        program.use()
        program.setUniformMat4("projection", proj.transpose_())
        program.setUniformMat4("view", view.transpose_())
        //cube
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture1.getId())
        cubePositions.forEachIndexed { index, vec3 ->
            val model = glm.translate(Mat4(), vec3) * glm.scale(
                Mat4(), Vec3(0.05  , 0.05, 0.05)
            )
            program.setUniformMat4("model", model)
            glDrawArrays(GL_TRIANGLES, 0, 36)
        }
        cube.disabledAttributes()
    }

    companion object {
        fun create(context: Context) = CubeRendering(
            context.resources.readRawTextFile(R.raw.cube_vertex_shader),
            context.resources.readRawTextFile(R.raw.cube_fragment_shader),
            Texture(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.raw.green,
                    BitmapFactory.Options().apply { inScaled = false })
            )
        )
    }
}