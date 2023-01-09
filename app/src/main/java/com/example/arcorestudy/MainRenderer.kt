package com.example.arcorestudy

import com.example.arcorestudy.MainRenderer.RenderCallback
import android.opengl.GLSurfaceView
import com.example.arcorestudy.CameraPreview
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import com.example.arcorestudy.MainRenderer
import com.google.ar.core.Frame
import com.google.ar.core.Session
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(callback: RenderCallback) : GLSurfaceView.Renderer {
    var isViewportChanged = false
        private set
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private val mCamera: CameraPreview?
    private val mRenderCallback: RenderCallback

    interface RenderCallback {
        fun preRender()
    }

    init {
        mCamera = CameraPreview()
        mRenderCallback = callback
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        mCamera!!.init()
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        isViewportChanged = true
        mViewportWidth = width
        mViewportHeight = height
    }

    override fun onDrawFrame(gl10: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mRenderCallback.preRender()
        GLES20.glDepthMask(false)
        mCamera!!.draw()
        GLES20.glDepthMask(true)
    }

    val textureId: Int
        get() = mCamera?.textureId ?: -1

    fun onDisplayChanged() {
        isViewportChanged = true
    }

    fun updateSession(session: Session, displayRotation: Int) {
        if (isViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight)
            isViewportChanged = false
        }
    }

    fun transformDisplayGeometry(frame: Frame?) {
        mCamera!!.transformDisplayGeometry(frame!!)
    }

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}