package com.example.arcorestudy

import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES30.GL_ARRAY_BUFFER
import android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES30.glBindBuffer
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glBufferData
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGenBuffers
import android.opengl.GLES30.glGenVertexArrays
import android.opengl.GLES30.glVertexAttribDivisor
import android.opengl.GLES30.glVertexAttribPointer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VertexData(
    private val vertices: FloatBuffer,
    private val indices: IntBuffer?,
    private val stride: Int,
    private val drawMode: Int = GL_STATIC_DRAW,
) {
    private val attributes = mutableListOf<Attribute>()

    private var vaoId: Int? = null

    fun bind() {
        val vbo = IntBuffer.allocate(1)
        glGenBuffers(1, vbo)
        val vao = IntBuffer.allocate(1)
        glGenVertexArrays(1, vao)

        vao[0].also {
            vaoId = it
            glBindVertexArray(it)
        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(
            GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertices.capacity(),
            vertices,
            drawMode
        )

        applyAttributes()
        bindIndices()

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    private fun applyAttributes() = attributes.forEach { attribute ->
        glEnableVertexAttribArray(attribute.location)
        glVertexAttribPointer(
            attribute.location,
            attribute.size,
            GL_FLOAT,
            false,
            (attribute.stride ?: stride) * Float.SIZE_BYTES,
            attribute.offset * Float.SIZE_BYTES
        )
        attribute.divisor?.also { glVertexAttribDivisor(attribute.location, it) }
    }

    private fun bindIndices() = indices?.takeIf { it.capacity() > 0 }?.also {
        val ebo = IntBuffer.allocate(1)
        glGenBuffers(1, ebo)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            Int.SIZE_BYTES * indices.capacity(),
            indices,
            drawMode
        )
    }

    data class Attribute(
        val location: Int,
        val size: Int,
        val offset: Int,
        val stride: Int? = null,
        val divisor: Int? = null,
    )

    companion object {
        fun apply(location: Int, size: Int, buffer: FloatBuffer) {
            glEnableVertexAttribArray(location)
            glVertexAttribPointer(
                location,
                size,
                GL_FLOAT,
                false,
                0,
                buffer
            )
        }
    }
}