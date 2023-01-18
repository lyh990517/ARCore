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
) {
    private lateinit var program: Program
    private var cube: VBOData =
        VBOData(cubeVertices, GL_STATIC_DRAW, 5)
    private var proj = Mat4()
    private var view = Mat4()
    private var cubePositions = mutableListOf<Vec3>()

    fun init() {
        texture1.load()
        program = Program.create(
            vertexShaderCode = vertexShaderCode,
            fragmentShaderCode = fragmentShaderCode
        )
        cube.bind()
        cube.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        cube.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
    }

    fun draw() {
        try {
            cube.draw()
            program.use()
            program.setUniformMat4("projection", proj)
            program.setUniformMat4("view", view)
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, texture1.getId())
            cubePositions.forEachIndexed { index, vec3 ->
                val model = glm.translate(Mat4(), vec3) * glm.scale(
                    Mat4(), Vec3(0.05, 0.05, 0.05)
                )
                program.setUniformMat4("model", model)
                glDrawArrays(GL_TRIANGLES, 0, 36)
            }
            cube.disabledAttributes()
        } catch (e: Exception) {
            Log.e("error", "${e.message}")
        } finally {
            Log.e("error", "error")
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    fun addPosition(vec3: Vec3) {
        cubePositions.add(vec3)
    }
    fun clear(){
        cubePositions.clear()
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