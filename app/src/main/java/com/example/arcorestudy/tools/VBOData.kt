package com.example.arcorestudy.tools

import android.opengl.GLES30.*
import com.example.gllibrary.Attribute
import com.example.gllibrary.VertexData
import com.example.gllibrary.toFloatBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class VBOData(
    private val vertex: FloatBuffer,
    private val indices: ShortBuffer? = null,
    private val drawMode: Int = GL_STATIC_DRAW,
    private val stride: Int
) {
    constructor(
        vertex: FloatArray,
        drawMode: Int = GL_STATIC_DRAW,
        stride: Int
    ) : this(vertex.toFloatBuffer(), null, drawMode, stride)

    private var vboId = -1
    private var eboId = -1
    private val attributes = mutableListOf<Attribute>()

    fun addAttribute(location: Int, size: Int, offset: Int) {
        attributes.add(
            Attribute(
                location = location,
                size = size,
                offset = offset
            )
        )
    }

    fun bindIndices() = indices?.takeIf { it.capacity() > 0 }?.also {
        val ebo = IntBuffer.allocate(1)
        eboId = ebo[0]
        glGenBuffers(1, ebo)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            Short.SIZE_BYTES * indices.capacity(),
            indices,
            drawMode
        )
    }

    fun getVBO() = vboId
    fun getEBO() = eboId
    fun bind() {
        val vbo = IntBuffer.allocate(1)
        glGenBuffers(1, vbo)
        vbo[0].let {
            glBindBuffer(GL_ARRAY_BUFFER, it)
            vboId = it
        }
        glBufferData(
            GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertex.capacity(),
            vertex,
            drawMode
        )
        applyAttributes()
        bindIndices()

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun applyAttributes() = attributes.forEach { attribute ->
        glEnableVertexAttribArray(attribute.location)
        glVertexAttribPointer(
            attribute.location,
            attribute.size,
            GL_FLOAT,
            false,
            stride * Float.SIZE_BYTES,
            attribute.offset * Float.SIZE_BYTES
        )
    }

    fun disabledAttributes() = attributes.forEach { attribute ->
        glDisableVertexAttribArray(attribute.location)
    }

    fun draw() {
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,eboId)
    }
}
