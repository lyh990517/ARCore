package com.example.arcorestudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30.*
import android.util.Log
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import com.example.arcorestudy.tools.VBOData
import com.example.arcorestudy.tools.cubeVertices

class CubeRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
    private val texture1: Texture,
) : Scene() {
    private lateinit var program: Program
    private var cube: VBOData =
        VBOData(cubeVertices, GL_STATIC_DRAW, 5)
    private var width: Int = 0
    private var height: Int = 0
    var proj = Mat4()
    var view = Mat4()
    var cubePositions = mutableListOf<Vec3>()

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
        try {
            cube.draw()
            program.use()
            program.setUniformMat4("projection", proj.transpose_())
            program.setUniformMat4("view", view.transpose_())
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
        }catch (e: Exception){
            Log.e("error","${e.message}")
        }
        finally {
            Log.e("error","error")
        }
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