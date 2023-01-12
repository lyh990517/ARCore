package com.example.arcorestudy

import android.opengl.GLES11Ext
import android.opengl.GLES30.*
import com.example.gllibrary.*
import com.google.ar.core.Frame
import java.nio.FloatBuffer

class CameraPreview {
    private val vertexShaderString = """
#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

out vec2 TexCoord;

void main() {
    gl_Position =  vec4(aPos, 1.0);
    TexCoord = aTexCoord;
}"""
    private val fragmentShaderString = """
#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
uniform samplerExternalOES sTexture;
in vec2 TexCoord;
out vec4 FragColor;
void main() {
    FragColor = texture(sTexture, TexCoord);
}"""

    private val mVertices: FloatBuffer
    private val mTexCoords: FloatBuffer
    private val mTexCoordsTransformed: FloatBuffer
    private val cameraTexture = CameraTexture()
    private lateinit var program: Program
    private val vertexData: VertexData

    init {
        mVertices = createFloatBuffer(QUAD_COORDS.size * java.lang.Float.SIZE / 8)
        mVertices.put(QUAD_COORDS)
        mVertices.position(0)
        mTexCoords = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)
        mTexCoords.put(QUAD_TEXCOORDS)
        mTexCoords.position(0)
        mTexCoordsTransformed = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)
        vertexData = VertexData(vertex, null, 5)
    }

    fun init() {
        cameraTexture.load()
        program = Program.create(vertexShaderString, fragmentShaderString)
        vertexData.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
        vertexData.bind()
        program.use()
    }

    fun draw() {
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture.getId())
        glBindVertexArray(vertexData.getVaoId())
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

    }

    val textureId: Int
        get() = cameraTexture.getId()

    fun transformDisplayGeometry(frame: Frame) {
        frame.transformDisplayUvCoords(mTexCoords, mTexCoordsTransformed)
    }

    companion object {
        private val QUAD_COORDS =
            floatArrayOf(
                -1.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f
            )
        private val QUAD_TEXCOORDS = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        private val vertex =
            floatArrayOf(
                -1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 0.0f, 0.0f
            )
    }
}