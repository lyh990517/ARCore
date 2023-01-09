package com.example.arcorestudy

import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import com.google.ar.core.Frame
import com.google.ar.core.PointCloud
import com.google.ar.core.Session
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(callback: RenderCallback) : GLSurfaceView.Renderer {
    var isViewportChanged = false
        private set
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private val mCamera: CameraRenderer?
    private val mPointCloud: PointCloudRenderer
    private val mRenderCallback: RenderCallback

    interface RenderCallback {
        fun preRender()
    }

    init {
        mCamera = CameraRenderer()
        mPointCloud = PointCloudRenderer()
        mRenderCallback = callback
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        mCamera?.init()
        mPointCloud.init()
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
        mCamera?.draw()
        GLES20.glDepthMask(true)
        mPointCloud.draw()
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
        frame?.let {
            mCamera?.transformDisplayGeometry(frame)
        }
    }

    fun updatePointCloud(pointCloud: PointCloud?) {
        mPointCloud.update(pointCloud)
    }

    fun setProjectionMatrix(matrix: FloatArray?) {
        mPointCloud.setProjectionMatrix(matrix)
    }

    fun updateViewMatrix(matrix: FloatArray?) {
        mPointCloud.setViewMatrix(matrix)
    }

}