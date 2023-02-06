package com.example.arcorestudy.tools

import android.opengl.GLES20
import android.opengl.GLES30
import com.example.gllibrary.Program
import com.example.gllibrary.createFloatBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

data class FaceMesh(
    val vertices: FloatBuffer,
    val normals: FloatBuffer,
    val texCoords: FloatBuffer,
    val indices: ShortBuffer,
) {
    val data: VBOData
    private val capacity = vertices.capacity() + texCoords.capacity()
    private val buffer = createFloatBuffer(capacity)

    init {

        while (vertices.hasRemaining()) {
            buffer.put(vertices.get())
            buffer.put(vertices.get())
            buffer.put(vertices.get())
            buffer.put(texCoords.get())
            buffer.put(texCoords.get())
        }
        buffer.position(0)
        data = VBOData(buffer, null, GLES30.GL_STATIC_DRAW, 5)
    }

    fun bind(program: Program) {
        data.bind()
        data.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        data.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
        bindIndices()
    }

    fun draw() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, data.getVBO())
        data.applyAttributes()
        GLES20.glDrawElements(GLES30.GL_TRIANGLES, indices.capacity(), GLES30.GL_UNSIGNED_INT, 0)
        data.disabledAttributes()
    }

    private fun bindIndices() = indices.takeIf { it.capacity() > 0 }?.also {
        val ebo = IntBuffer.allocate(1)
        GLES30.glGenBuffers(1, ebo)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            Int.SIZE_BYTES * indices.capacity(),
            indices,
            GLES30.GL_STATIC_DRAW
        )
    }
}