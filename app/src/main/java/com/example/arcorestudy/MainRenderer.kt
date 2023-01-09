package com.example.arcorestudy

import android.graphics.Color
import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.Matrix
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
    private val mPoint: Sphere
    private var mLineX: Line? = null
    private var mLineY: Line? = null
    private var mLineZ: Line? = null
    private val mProjMatrix = FloatArray(16)
    private val mRenderCallback: RenderCallback

    interface RenderCallback {
        fun preRender()
    }

    init {
        mCamera = CameraRenderer()
        mPointCloud = PointCloudRenderer()
        mPoint = Sphere(0.01f, Color.YELLOW)
        mRenderCallback = callback
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        mCamera!!.init()
        mPointCloud.init()
        mPoint.init()
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
        mPointCloud.draw()
        mPoint.draw()
        if (mLineX != null) {
            if (!mLineX!!.isInitialized) {
                mLineX!!.init()
            }
            mLineX!!.draw()
        }
        if (mLineY != null) {
            if (!mLineY!!.isInitialized) {
                mLineY!!.init()
            }
            mLineY!!.draw()
        }
        if (mLineZ != null) {
            if (!mLineZ!!.isInitialized) {
                mLineZ!!.init()
            }
            mLineZ!!.draw()
        }
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

    fun updatePointCloud(pointCloud: PointCloud?) {
        mPointCloud.update(pointCloud)
    }

    fun setProjectionMatrix(matrix: FloatArray?) {
        System.arraycopy(matrix, 0, mProjMatrix, 0, 16)
        mPointCloud.setProjectionMatrix(matrix)
        mPoint.setProjectionMatrix(matrix)
    }

    fun updateViewMatrix(matrix: FloatArray?) {
        mPointCloud.setViewMatrix(matrix)
        mPoint.setViewMatrix(matrix)
        if (mLineX != null) {
            mLineX!!.setViewMatrix(matrix)
        }
        if (mLineY != null) {
            mLineY!!.setViewMatrix(matrix)
        }
        if (mLineZ != null) {
            mLineZ!!.setViewMatrix(matrix)
        }
    }

    fun setModelMatrix(matrix: FloatArray?) {
        mPoint.setModelMatrix(matrix)
        mLineX!!.setModelMatrix(matrix)
        mLineY!!.setModelMatrix(matrix)
        mLineZ!!.setModelMatrix(matrix)
    }

    fun addPoint(x: Float, y: Float, z: Float) {
        val matrix = FloatArray(16)
        Matrix.setIdentityM(matrix, 0)
        Matrix.translateM(matrix, 0, x, y, z)
        mPoint.setModelMatrix(matrix)
    }

    fun addLineX(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
        mLineX = Line(x1, y1, z1, x2, y2, z2, 10, Color.RED)
        mLineX!!.setProjectionMatrix(mProjMatrix)
        val identity = FloatArray(16)
        Matrix.setIdentityM(identity, 0)
        mLineX!!.setModelMatrix(identity)
    }

    fun addLineY(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
        mLineY = Line(x1, y1, z1, x2, y2, z2, 10, Color.GREEN)
        mLineY!!.setProjectionMatrix(mProjMatrix)
        val identity = FloatArray(16)
        Matrix.setIdentityM(identity, 0)
        mLineY!!.setModelMatrix(identity)
    }

    fun addLineZ(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
        mLineZ = Line(x1, y1, z1, x2, y2, z2, 10, Color.BLUE)
        mLineZ!!.setProjectionMatrix(mProjMatrix)
        val identity = FloatArray(16)
        Matrix.setIdentityM(identity, 0)
        mLineZ!!.setModelMatrix(identity)
    }

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}