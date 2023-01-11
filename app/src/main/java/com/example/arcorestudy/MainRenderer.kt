package com.example.arcorestudy

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.arcorestudy.CameraPreview
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import com.example.arcorestudy.MainRenderer
import com.google.ar.core.Frame
import com.google.ar.core.Session
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(private val context: Context, private val sessionManager: SessionManager) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        sessionManager.mCamera!!.init()
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        sessionManager.isViewportChanged = true
        mViewportWidth = width
        mViewportHeight = height
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onDrawFrame(gl10: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        preRender()
        GLES20.glDepthMask(false)
        sessionManager.mCamera!!.draw()
        GLES20.glDepthMask(true)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() {

        if (sessionManager.isViewportChanged) {
            val display: Display = context.display!!
            val displayRotation = display.rotation
            updateSession(sessionManager.mSession!!, displayRotation)
        }
        sessionManager.mSession?.setCameraTextureName(textureId)
        val frame = sessionManager.mSession!!.update()
        if (frame.hasDisplayGeometryChanged()) {
            transformDisplayGeometry(frame)
        }
    }

    val textureId: Int
        get() = sessionManager.mCamera?.textureId ?: -1

    fun onDisplayChanged() {
        sessionManager.isViewportChanged = true
    }

    fun updateSession(session: Session, displayRotation: Int) {
        if (sessionManager.isViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight)
            sessionManager.isViewportChanged = false
        }
    }

    fun transformDisplayGeometry(frame: Frame?) {
        sessionManager.mCamera!!.transformDisplayGeometry(frame!!)
    }

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}