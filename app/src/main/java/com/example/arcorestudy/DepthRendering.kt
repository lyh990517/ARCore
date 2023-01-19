package com.example.arcorestudy

import android.content.Context
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLES30.*
import com.example.gllibrary.Program
import com.example.gllibrary.createFloatBuffer
import com.example.gllibrary.readRawTextFile
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame

class DepthRendering(
    private val vShader: String,
    private val fShader: String,
    private val depthTextureID: Int
) {

    private lateinit var program: Program
    private var depthTextureParam: Int = -1
    private var depthTextureId = depthTextureID
    private var depthQuadPositionParams = -1
    private var depthQuadTexCoordParams = -1
    private val quadCoord = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, +1.0f,
        1.0f, 1.0f
    )
    private val quadCoords = createFloatBuffer(8)
    private val quadTexCoords = createFloatBuffer(8)

    fun init() {
        program = Program.create(vShader, fShader)
        quadCoords.put(quadCoord)
        quadCoords.position(0)
    }

    fun draw(frame: Frame) {
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadCoords,
                Coordinates2d.TEXTURE_NORMALIZED,
                quadTexCoords
            )
        }

        if (frame.timestamp == 0L || depthTextureID == -1) {
            return
        }
        quadTexCoords.position(0)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, depthTextureId)
        program.use()
        glVertexAttribPointer(program.getUniformLocation("aPos"), 2, GL_FLOAT, false, 0, quadCoords)
        glVertexAttribPointer(
            program.getUniformLocation("aTexCoord"),
            2,
            GL_FLOAT,
            false,
            0,
            quadTexCoords
        )
        glEnableVertexAttribArray(program.getUniformLocation("aPos"))
        glEnableVertexAttribArray(program.getUniformLocation("aTexCoord"))
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glDisableVertexAttribArray(program.getUniformLocation("aPos"))
        glDisableVertexAttribArray(program.getUniformLocation("aTexCoord"))
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)

    }

    companion object {
        fun create(context: Context, depthTextureID: Int) = DepthRendering(
            context.resources.readRawTextFile(R.raw.depth_image_vertex),
            context.resources.readRawTextFile(R.raw.depth_image_fragment),
            depthTextureID
        )
    }
}