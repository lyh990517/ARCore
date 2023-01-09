package com.example.arcorestudy

import android.graphics.Color
import com.example.arcorestudy.MainRenderer.RenderCallback
import android.opengl.GLSurfaceView
import com.example.arcorestudy.CameraRenderer
import com.example.arcorestudy.PointCloudRenderer
import com.example.arcorestudy.Sphere
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.Matrix
import com.google.ar.core.PointCloud
import com.example.arcorestudy.MainRenderer
import com.google.ar.core.Frame
import com.google.ar.core.Session
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(callback: RenderCallback) : GLSurfaceView.Renderer {
    var isViewportChanged = false
        private set
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private val mCamera: CameraRenderer?
    private val mPointCloud: PointCloudRenderer
    private val mShperes: MutableList<Sphere> = ArrayList()
    private val mPoints: MutableList<FloatArray> = ArrayList()
    private val mLines: MutableList<Line> = ArrayList()
    private val mProjMatrix = FloatArray(16)
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
        mCamera!!.init()
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
        mCamera!!.draw()
        GLES20.glDepthMask(true)
        mPointCloud.draw()
        for (i in mShperes.indices) {
            val sphere = mShperes[i]
            if (!sphere.isInitialized) {
                sphere.init()
            }
            sphere.draw()
        }
        for (i in mLines.indices) {
            val line = mLines[i]
            if (!line.isInitialized) {
                line.init()
            }
            line.draw()
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
    }

    fun updateViewMatrix(matrix: FloatArray?) {
        mPointCloud.setViewMatrix(matrix)
        for (i in mShperes.indices) {
            mShperes[i].setViewMatrix(matrix)
        }
        for (i in mLines.indices) {
            mLines[i].setViewMatrix(matrix)
        }
    }

    fun setModelMatrix(matrix: FloatArray?) {}
    fun addPoint(point: FloatArray): Int {
        mPoints.add(point)
        val currentPoint = Sphere(0.01f, Color.GREEN)
        currentPoint.setProjectionMatrix(mProjMatrix)
        val translation = FloatArray(16)
        Matrix.setIdentityM(translation, 0)
        Matrix.translateM(translation, 0, point[0], point[1], point[2])
        currentPoint.setModelMatrix(translation)
        mShperes.add(currentPoint)
        if (mPoints.size >= 2) {
            val start = mPoints[mPoints.size - 2]
            val end = mPoints[mPoints.size - 1]
            val currentLine =
                Line(start[0], start[1], start[2], end[0], end[1], end[2], 10, Color.YELLOW)
            currentLine.setProjectionMatrix(mProjMatrix)
            val identity = FloatArray(16)
            Matrix.setIdentityM(identity, 0)
            currentLine.setModelMatrix(identity)
            mLines.add(currentLine)
        }
        return mShperes.size
    }

    fun removePoint(): Int {
        if (mPoints.size >= 1) {
            mPoints.removeAt(mPoints.size - 1)
        }
        if (mShperes.size >= 1) {
            mShperes.removeAt(mShperes.size - 1)
        }
        if (mLines.size >= 1) {
            mLines.removeAt(mLines.size - 1)
        }
        return mShperes.size
    }

    companion object {
        private val TAG = MainRenderer::class.java.simpleName
    }
}