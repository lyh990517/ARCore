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

class MainRenderer(private val sessionManager: SessionManager) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        sessionManager.mCamera!!.init(width, height)
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
        sessionManager.updateSession(mViewportWidth, mViewportHeight)
        sessionManager.mSession?.setCameraTextureName(textureId)
        sessionManager.mSession!!.update().run {
            if (this.hasDisplayGeometryChanged()) {
                sessionManager.mCamera!!.transformDisplayGeometry(this)
            }
        }
    }

    val textureId: Int
        get() = sessionManager.mCamera?.textureId ?: -1

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}