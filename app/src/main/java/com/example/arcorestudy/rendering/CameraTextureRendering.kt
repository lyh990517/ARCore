package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import android.opengl.GLES30.glBindTexture
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import com.example.arcorestudy.CameraTexture
import com.example.arcorestudy.Program
import com.example.arcorestudy.R
import com.example.arcorestudy.VertexData
import com.example.arcorestudy.createFloatBuffer
import com.example.arcorestudy.readRawTextFile
import com.google.ar.core.Frame
import java.nio.FloatBuffer

class CameraTextureRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
){

    private val mVertices: FloatBuffer
    private val mTexCoords: FloatBuffer
    private val mTexCoordsTransformed: FloatBuffer
    private val cameraTexture = CameraTexture()
    private lateinit var program: Program

    init {
        mVertices = createFloatBuffer(QUAD_COORDS.size * java.lang.Float.SIZE / 8)
        mVertices.put(QUAD_COORDS)
        mVertices.position(0)
        mTexCoords = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)
        mTexCoords.put(QUAD_TEXCOORDS)
        mTexCoords.position(0)
        mTexCoordsTransformed = createFloatBuffer(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8)

    }

    fun draw() {
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture.getId())
        program.use()
        VertexData.apply(program.getAttributeLocation("aPosition"),3,mVertices)
        VertexData.apply(program.getAttributeLocation("aTexCoord"),2,mTexCoordsTransformed)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glDisableVertexAttribArray(program.getAttributeLocation("aPosition"))
        glDisableVertexAttribArray(program.getAttributeLocation("aTexCoord"))
    }

    fun init() {
        cameraTexture.load()
        program = Program.create(vertexShaderCode, fragmentShaderCode)
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
        fun create(context: Context): CameraTextureRendering {
            val resource = context.resources
            return CameraTextureRendering(
                resource.readRawTextFile(R.raw.camera_vertex),
                resource.readRawTextFile(R.raw.camera_fragment)
            )
        }
    }
}