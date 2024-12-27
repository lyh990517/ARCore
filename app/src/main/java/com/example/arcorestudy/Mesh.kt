package com.example.arcorestudy

import android.opengl.GLES30.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

data class Mesh(
    val vertices: FloatBuffer,
    val normals: FloatBuffer,
    val texCoords: FloatBuffer,
    val indices: IntBuffer,
) {
    private val data: VBOData
    private val capacity = vertices.capacity() + texCoords.capacity()
    private val buffer = createFloatBuffer(capacity)

    init {
        while (vertices.hasRemaining() && texCoords.hasRemaining()) {
            buffer.put(vertices.get())   // x
            buffer.put(vertices.get())   // y
            buffer.put(vertices.get())   // z
            buffer.put(texCoords.get())  // u
            buffer.put(texCoords.get())  // v
        }
        buffer.position(0)
        data = VBOData(buffer, GL_STATIC_DRAW, 5)
    }

    fun bind(program: Program) {
        data.bind()
        data.addAttribute(glGetAttribLocation(program.getProgram(), "aPos"), 3, 0)
        data.addAttribute(glGetAttribLocation(program.getProgram(), "aTexCoord"), 2, 3)
        bindIndices()
    }

    fun draw() {
        glBindBuffer(GL_ARRAY_BUFFER, data.getVBO())
        data.applyAttributes()
        glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0)
        data.disabledAttributes()
    }

    private fun bindIndices() = indices.takeIf { it.capacity() > 0 }?.also {
        val ebo = IntBuffer.allocate(1)
        glGenBuffers(1, ebo)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            Int.SIZE_BYTES * indices.capacity(),
            indices,
            GL_STATIC_DRAW
        )
    }
}