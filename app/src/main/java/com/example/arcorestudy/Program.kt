package com.example.arcorestudy

import android.opengl.GLES20.*
import android.util.Log
import glm_.mat4x4.Mat4
import java.nio.IntBuffer

class Program(private val vertexShader: Int, private val fragmentShader: Int) {
    private var program: Int = -1
    private var attributes = mutableMapOf<String, Int>()
    private var uniforms = mutableMapOf<String, Int>()
    fun getAttributeLocation(name: String) = attributes.getValue(name)
    fun getUniformLocation(name: String) = uniforms.getValue(name)
    fun setUniformMat4(name: String, matrix: Mat4) {
        glUniformMatrix4fv(
            getUniformLocation(name),
            1,
            false,
            toFloatArray(matrix),
            0
        )
    }

    fun link() {
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
    }

    private fun fetchAttributes() {
        val count = IntBuffer.allocate(1)
        glGetProgramiv(program, GL_ACTIVE_ATTRIBUTES, count) //프로그램의 정보를 가져옵니다.
        for (i in 0 until count[0]) {
            val name = glGetActiveAttrib(program, i, IntBuffer.allocate(1), IntBuffer.allocate(1))
            val location = glGetAttribLocation(program, name)
            attributes[name] = location
        }
    }

    private fun fetchUniforms() {

        val count = IntBuffer.allocate(1)
        glGetProgramiv(program, GL_ACTIVE_UNIFORMS, count) //프로그램의 정보를 가져옵니다.
        for (i in 0 until count[0]) {
            val name = glGetActiveUniform(program, i, IntBuffer.allocate(1), IntBuffer.allocate(1))
            val location = glGetUniformLocation(program, name)
            uniforms[name] = location
        }
    }

    fun use() = glUseProgram(program)

    companion object {
        fun create(vertexShaderCode: String, fragmentShaderCode: String): Program {
            var vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
            var fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            return Program(vertexShader, fragmentShader).apply {
                link()
                fetchAttributes()
                fetchUniforms()
                glDeleteShader(vertexShader)
                glDeleteShader(fragmentShader)
            }
        }
    }
}