package com.example.arcorestudy

import android.opengl.GLES11Ext
import android.opengl.GLES30.*
import com.example.gllibrary.*
import com.google.ar.core.Frame
import java.nio.FloatBuffer

class CameraPreview {
    private val vertexShaderString = """
attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
void main() {
   vTexCoord = aTexCoord;
   gl_Position = aPosition;
}"""
    private val fragmentShaderString = """
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES sTexture;
varying vec2 vTexCoord;
void main() {
    gl_FragColor = texture2D(sTexture, vTexCoord);
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
        //텍스쳐
        cameraTexture.load()

        //쉐이더
        program = Program.create(vertexShaderString, fragmentShaderString)
        vertexData.addAttribute(program.getAttributeLocation("aPosition"), 3, 0)
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