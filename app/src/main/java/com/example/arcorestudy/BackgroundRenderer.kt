/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.arcorestudy

import android.content.Context
import kotlin.Throws
import android.opengl.GLES20
import android.opengl.GLES11Ext
import com.example.arcorestudy.BackgroundRenderer
import com.example.arcorestudy.tools.DepthTexture
import com.example.gllibrary.Program
import com.example.gllibrary.createFloatBuffer
import com.example.gllibrary.readRawTextFile
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * This class renders the AR background from camera feed. It creates and hosts the texture given to
 * ARCore to be filled with the camera image.
 */
class BackgroundRenderer(
    private val vShader: String,
    private val fShader: String
) {
    private var quadCoords: FloatBuffer? = null
    private var quadTexCoords: FloatBuffer? = null
    private lateinit var quadProgram: Program
    private var quadPositionParam = 0
    private var quadTexCoordParam = 0
    var textureId = -1
        private set
    private lateinit var depthProgram: Program
    private var depthTextureParam = 0
    private var depthTextureId = -1
    private var depthQuadPositionParam = 0
    private var depthQuadTexCoordParam = 0
    private var depthRangeToRenderMm = 0.0f
    private var depthRangeToRenderMmParam = 0
    val depthTexture = DepthTexture()

    /**
     * Allocates and initializes OpenGL resources needed by the background renderer. Must be called on
     * the OpenGL thread, typically in [GLSurfaceView.Renderer.onSurfaceCreated].
     *
     * @param context Needed to access shader source.
     */
    fun createOnGlThread() {
        // Generate the background texture.
        depthTexture.createTexture()
        quadCoords = createFloatBuffer(8)
        quadCoords!!.put(QUAD_COORDS)
        quadCoords!!.position(0)
        quadTexCoords = createFloatBuffer(8)
        quadProgram = Program.create(vShader, fShader)
        quadPositionParam = GLES20.glGetAttribLocation(quadProgram.getProgram(), "a_Position")
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram.getProgram(), "a_TexCoord")
    }

    fun createDepthShaders() {
        // Loads shader for rendering depth map.
        depthProgram = Program.create(vShader, fShader)
        depthTextureParam = GLES20.glGetUniformLocation(depthProgram.getProgram(), "u_Depth")
        depthRangeToRenderMmParam =
            GLES20.glGetUniformLocation(depthProgram.getProgram(), "u_DepthRangeToRenderMm")
        depthQuadPositionParam = GLES20.glGetAttribLocation(depthProgram.getProgram(), "a_Position")
        depthQuadTexCoordParam = GLES20.glGetAttribLocation(depthProgram.getProgram(), "a_TexCoord")
        this.depthTextureId = depthTexture.getDepthTexture()
    }

    /**
     * Draws the AR background image. The image will be drawn such that virtual content rendered with
     * the matrices provided by [com.google.ar.core.Camera.getViewMatrix] and
     * [com.google.ar.core.Camera.getProjectionMatrix] will
     * accurately follow static physical objects. This must be called **before** drawing virtual
     * content.
     *
     * @param frame The current `Frame` as returned by [Session.update].
     */
    fun draw(frame: Frame) {
        // If display rotation changed (also includes view size change), we need to re-query the uv
        // coordinates for the screen rect, as they may have changed as well.
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadCoords,
                Coordinates2d.TEXTURE_NORMALIZED,
                quadTexCoords
            )
        }
        if (frame.timestamp == 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            return
        }
        draw()
    }

    /**
     * Draws the camera background image using the currently configured [ ][BackgroundRenderer.quadTexCoords] image texture coordinates.
     */
    private fun draw() {
        // Ensure position is rewound before use.
        quadTexCoords!!.position(0)

        // No need to test or write depth, the screen quad has arbitrary depth, and is expected
        // to be drawn first.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUseProgram(quadProgram.getProgram())

        // Set the vertex positions.
        GLES20.glVertexAttribPointer(
            quadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords
        )

        // Set the texture coordinates.
        GLES20.glVertexAttribPointer(
            quadTexCoordParam, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords
        )

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(quadPositionParam)
        GLES20.glEnableVertexAttribArray(quadTexCoordParam)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(quadPositionParam)
        GLES20.glDisableVertexAttribArray(quadTexCoordParam)

        // Restore the depth state for further drawing.
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    fun drawDepth(frame: Frame) {
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadCoords,
                Coordinates2d.TEXTURE_NORMALIZED,
                quadTexCoords
            )
        }
        if (frame.timestamp == 0L || depthTextureId == -1) {
            return
        }

        // Ensure position is rewound before use.
        quadTexCoords!!.position(0)

        // No need to test or write depth, the screen quad has arbitrary depth, and is expected
        // to be drawn first.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureId)
        GLES20.glUseProgram(depthProgram.getProgram())
        GLES20.glUniform1i(depthTextureParam, 0)
        depthRangeToRenderMm += 50.0f
        if (depthRangeToRenderMm > MAX_DEPTH_RANGE_TO_RENDER_MM) {
            depthRangeToRenderMm = 0.0f
        }
        GLES20.glUniform1f(depthRangeToRenderMmParam, depthRangeToRenderMm)

        // Set the vertex positions and texture coordinates.
        GLES20.glVertexAttribPointer(
            depthQuadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords
        )
        GLES20.glVertexAttribPointer(
            depthQuadTexCoordParam, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords
        )

        // Draws the quad.
        GLES20.glEnableVertexAttribArray(depthQuadPositionParam)
        GLES20.glEnableVertexAttribArray(depthQuadTexCoordParam)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(depthQuadPositionParam)
        GLES20.glDisableVertexAttribArray(depthQuadTexCoordParam)

        // Restore the depth state for further drawing.
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    companion object {
        private val TAG = BackgroundRenderer::class.java.simpleName
        fun create(context: Context): BackgroundRenderer {
            val resource = context.resources
            return BackgroundRenderer(
                resource.readRawTextFile(R.raw.dmv),
                resource.readRawTextFile(R.raw.dmf)
            )
        }

        // Shader names.
        private const val CAMERA_VERTEX_SHADER_NAME = "shaders/screenquad.vert"
        private const val CAMERA_FRAGMENT_SHADER_NAME = "shaders/screenquad.frag"
        private const val DEPTH_VERTEX_SHADER_NAME = "shaders/background_show_depth_map.vert"
        private const val DEPTH_FRAGMENT_SHADER_NAME = "shaders/background_show_depth_map.frag"
        private const val COORDS_PER_VERTEX = 2
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private const val MAX_DEPTH_RANGE_TO_RENDER_MM = 20000.0f

        /**
         * (-1, 1) ------- (1, 1)
         * |    \           |
         * |       \        |
         * |          \     |
         * |             \  |
         * (-1, -1) ------ (1, -1)
         * Ensure triangles are front-facing, to support glCullFace().
         * This quad will be drawn using GL_TRIANGLE_STRIP which draws two
         * triangles: v0->v1->v2, then v2->v1->v3.
         */
        private val QUAD_COORDS = floatArrayOf(
            -1.0f, -1.0f, +1.0f, -1.0f, -1.0f, +1.0f, +1.0f, +1.0f
        )
    }
}