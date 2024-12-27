package com.example.arcorestudy

import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
import android.opengl.GLES30.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES30.GL_DEPTH_TEST
import android.opengl.GLES30.glClear
import android.opengl.GLES30.glDepthMask
import android.opengl.GLES30.glEnable
import android.opengl.GLES30.glViewport
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.PointCloud
import com.google.ar.core.exceptions.SessionPausedException
import glm_.vec3.Vec3
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainRenderer(
    private val sessionManager: SessionManager,
    private val renderingManager: RenderingManager,
) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private var currentX = 0f
    private var currentY = 0f
    var onTouch = false

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) = with(renderingManager) {
        mCamera.init()
        arObjectScene.init()
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) = with(sessionManager) {
        glViewport(0, 0, width, height)
        isViewportChanged = true
        mViewportWidth = width
        mViewportHeight = height
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onDrawFrame(gl10: GL10) {
        glEnable(GL_DEPTH_TEST)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        preRender()
        render()
    }

    private fun render() = with(renderingManager) {
        glDepthMask(false)
        mCamera.draw()
        glDepthMask(true)
        arObjectScene.draw()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() = with(sessionManager) {
        updateSession(mViewportWidth, mViewportHeight)
        mSession?.setCameraTextureName(textureId)
        try {
            val frame = mSession!!.update()
            if (frame.hasDisplayGeometryChanged()) {
                renderingManager.mCamera.transformDisplayGeometry(frame)
            }
            renderPointCloud(frame)
            extractMatrixFromCamera(frame).let { setMatrix(it.first, it.second) }
            getHitPose(frame)
        } catch (_: SessionPausedException) {

        }
    }

    private fun getHitPose(frame: Frame) {
        val results = frame.hitTest(currentX, currentY).filter {
            val trackable = it.trackable
            trackable is DepthPoint
        }
        results.forEach {
            val pose = it.hitPose
            addPoint(Vec3(pose.tx(), pose.ty(), pose.tz()))
        }
    }

    private fun addPoint(vec3: Vec3) = with(renderingManager) {
        if (onTouch) {
            arObjectScene.addPosition(vec3)
        }
    }

    private fun setMatrix(projection: FloatArray, view: FloatArray) = with(renderingManager) {
        arObjectScene.setProjectionMatrix(projection)
        arObjectScene.setViewMatrix(view)
    }

    private fun extractMatrixFromCamera(frame: Frame): Pair<FloatArray, FloatArray> {
        val camera = frame.camera
        val projMatrix = FloatArray(16)
        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)
        val viewMatrix = FloatArray(16)
        camera.getViewMatrix(viewMatrix, 0)
        return Pair(projMatrix, viewMatrix)
    }

    private fun renderPointCloud(frame: Frame) = with(renderingManager) {
        val pointCloud: PointCloud = frame.acquirePointCloud()
        mPointCloud.update(pointCloud)
        pointCloud.release()
    }

    fun getXY(x: Float, y: Float) {
        currentX = x
        currentY = y
    }

    private val textureId: Int
        get() = renderingManager.mCamera.textureId ?: -1

}