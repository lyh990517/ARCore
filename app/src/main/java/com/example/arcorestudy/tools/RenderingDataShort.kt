package com.example.arcorestudy.tools

import android.opengl.GLES20
import android.opengl.GLES30
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class RenderingDataShort (
    private val vertices: FloatBuffer,
    private val indices: ShortBuffer?,
    private val stride: Int,
    private val drawMode: Int = GLES20.GL_STATIC_DRAW
) {

    private val attributes = mutableListOf<Attribute>()

    private var vaoId: Int? = null
    fun addAttribute(location: Int, size: Int, offset: Int) {
        attributes.add(
            Attribute(
                location = location,
                size = size,
                offset = offset
            )
        )
    }

    fun bind() {
        val vbo = IntBuffer.allocate(1)
        GLES30.glGenBuffers(1, vbo)
        val vao = IntBuffer.allocate(1)
        GLES30.glGenVertexArrays(1, vao)

        vao[0].also {
            vaoId = it
            GLES30.glBindVertexArray(it)
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        vertices.position(0)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertices.capacity(),
            vertices,
            drawMode
        )

        applyAttributes()
        bindIndices()

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun getVaoId() = vaoId ?: throw IllegalStateException("Call bind() before accessing VAO")

    private fun applyAttributes() = attributes.forEach { attribute ->
        GLES30.glEnableVertexAttribArray(attribute.location)
        GLES30.glVertexAttribPointer(
            attribute.location,
            attribute.size,
            GLES20.GL_FLOAT,
            false,
            (attribute.stride ?: stride) * Float.SIZE_BYTES,
            attribute.offset * Float.SIZE_BYTES
        )
        attribute.divisor?.also { GLES30.glVertexAttribDivisor(attribute.location, it) }
    }

    private fun bindIndices() = indices?.takeIf { it.capacity() > 0 }?.also {
        val ebo = IntBuffer.allocate(1)
        GLES30.glGenBuffers(1, ebo)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            Short.SIZE_BYTES * indices.capacity(),
            indices,
            drawMode
        )
    }

    data class Attribute(
        val location: Int,
        val size: Int,
        val offset: Int,
        val stride: Int? = null,
        val divisor: Int? = null
    )
}