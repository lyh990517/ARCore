package com.example.arcorestudy

import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gllibrary.toFloatBuffer
import com.example.gllibrary.toMat4
import com.google.ar.core.Frame
import com.google.ar.core.PointCloud
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(private val sessionManager: SessionManager) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        sessionManager.mCamera!!.init()
        sessionManager.mPointCloud!!.init(0, 0)
        sessionManager.cubeScene!!.init(0, 0)
        sessionManager.isViewportChanged = true
        mViewportWidth = width
        mViewportHeight = height
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onDrawFrame(gl10: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        preRender()
        glDepthMask(false)
        sessionManager.mCamera!!.draw()
        glDepthMask(true)
        sessionManager.mPointCloud!!.draw()
        sessionManager.cubeScene!!.draw()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() {
        sessionManager.updateSession(mViewportWidth, mViewportHeight)
        sessionManager.mSession?.setCameraTextureName(textureId)
        sessionManager.mSession!!.update().run {
            if (this.hasDisplayGeometryChanged()) {
                sessionManager.mCamera!!.transformDisplayGeometry(this)
            }
            renderPointCloud()
            val (projMatrix, viewMatrix) = extractMatrixFromCamera()
            setMatrix(projMatrix, viewMatrix)
        }
    }

    private fun setMatrix(projection: FloatArray, view: FloatArray) {
        sessionManager.mPointCloud!!.setProjectionMatrix(projection)
        sessionManager.mPointCloud.setViewMatrix(view)
        sessionManager.cubeScene!!.view = view.toMat4()
        sessionManager.cubeScene.proj = projection.toMat4()
    }

    private fun Frame.extractMatrixFromCamera(): Pair<FloatArray, FloatArray> {
        val camera = this.camera
        val projMatrix = FloatArray(16)
        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)
        val viewMatrix = FloatArray(16)
        camera.getViewMatrix(viewMatrix, 0)
        return Pair(projMatrix, viewMatrix)
    }

    private fun Frame.renderPointCloud() {
        val pointCloud: PointCloud = this.acquirePointCloud()
        sessionManager.mPointCloud!!.update(pointCloud)
        pointCloud.release()
    }

    val textureId: Int
        get() = sessionManager.mCamera?.textureId ?: -1

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}