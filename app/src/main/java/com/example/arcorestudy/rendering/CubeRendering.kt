package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30.*
import com.example.arcorestudy.R
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import com.example.arcorestudy.tools.VBOData
import com.example.arcorestudy.tools.cubeVertices

class CubeRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String,
) {
    private lateinit var program: Program
    private var cube: VBOData =
        VBOData(cubeVertices, GL_STATIC_DRAW, 3)
    private var proj = Mat4()
    private var view = Mat4()
    private var cubePositions = mutableListOf<Vec3>()
    private var red = 0f
    private var green = 0f
    private var blue = 0f
    var size = 0.05f
    fun init() {
        program = Program.create(
            vertexShaderCode = vertexShaderCode,
            fragmentShaderCode = fragmentShaderCode
        )
        cube.bind()
        cube.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
    }

    fun draw() {
        try {
            cube.draw()
            program.use()
            program.setUniformMat4("projection", proj)
            program.setUniformMat4("view", view)
            glUniform4f(program.getUniformLocation("vColor"), red, green, blue, 1f)
            try {
                cubePositions.forEachIndexed { index, vec3 ->
                    val model = glm.translate(Mat4(), vec3) * glm.scale(
                        Mat4(), Vec3(size, size, size)
                    )
                    program.setUniformMat4("model", model)
                    glDrawArrays(GL_TRIANGLES, 0, 36)
                }
                cube.disabledAttributes()
            } catch (e: Exception) {

            }
        } catch (e: Exception) {

        }
    }

    fun cubeRGB(red: Float, green: Float, blue: Float) {
        this.red = red
        this.green = green
        this.blue = blue
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

    fun clear() {
        cubePositions.clear()
    }

    companion object {
        fun create(context: Context) = CubeRendering(
            context.resources.readRawTextFile(R.raw.cube_vertex_shader),
            context.resources.readRawTextFile(R.raw.cube_fragment_shader),
        )
    }
}