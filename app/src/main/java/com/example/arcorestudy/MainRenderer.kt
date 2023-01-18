package com.example.arcorestudy

import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.gllibrary.toMat4
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.PointCloud
import com.google.ar.core.TrackingState
import glm_.vec3.Vec3
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(private val sessionManager: SessionManager) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private var currentX = 0f
    private var currentY = 0f
    var onTouch = false
    var mode = MutableLiveData("cube")
    val distanceLiveData = MutableLiveData<Float>()
    val planeLiveData = MutableLiveData<String>()

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        sessionManager.mCamera!!.init()
        sessionManager.mPointCloud!!.init()
        sessionManager.cubeScene!!.init()
        sessionManager.arObjectScene!!.init()
        sessionManager.isViewportChanged = true
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

    private fun render() {
        glDepthMask(false)
        sessionManager.mCamera!!.draw()
        glDepthMask(true)
        sessionManager.mPointCloud!!.draw()
        sessionManager.cubeScene!!.draw()
        sessionManager.arObjectScene!!.draw()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() {
        sessionManager.updateSession(mViewportWidth, mViewportHeight)
        sessionManager.mSession?.setCameraTextureName(textureId)
        val frame = sessionManager.mSession!!.update()
        if (frame.hasDisplayGeometryChanged()) {
            sessionManager.mCamera!!.transformDisplayGeometry(frame)
        }
        renderPointCloud(frame)
        extractMatrixFromCamera(frame).let { setMatrix(it.first, it.second) }
        getHitPose(frame)
        detectPlane()
    }

    private fun detectPlane() {
        var isPlane = false
        val planes = sessionManager.mSession!!.getAllTrackables(Plane::class.java)
        planes.forEach { plane ->
            if (plane.trackingState == TrackingState.TRACKING && plane.subsumedBy == null) {
                isPlane = true
            }
        }
        if (isPlane) {
            planeLiveData.postValue("plane is detected!!")
        } else {
            planeLiveData.postValue("nothing detected!!")
        }
    }

    private fun getHitPose(frame: Frame) {
        val results = frame.hitTest(currentX, currentY)
        if (results.size > 0) {
            val distance = results[0].distance
            distanceLiveData.postValue(distance)
            val pose = results[0].hitPose
            addPoint(pose.tx(), pose.ty(), pose.tz())
        }
    }

    private fun addPoint(x: Float, y: Float, z: Float) {
        if (onTouch) {
            when (mode.value) {
                "cube" -> {
                    sessionManager.cubeScene!!.cubePositions.add(Vec3(x, y, z))
                }
                "arObject" -> {
                    sessionManager.arObjectScene!!.objPosition.add(Vec3(x, y, z))
                }
            }
        }
    }

    private fun setMatrix(projection: FloatArray, view: FloatArray) {
        sessionManager.mPointCloud!!.setProjectionMatrix(projection)
        sessionManager.mPointCloud.setViewMatrix(view)
        sessionManager.cubeScene!!.setProjectionMatrix(projection)
        sessionManager.cubeScene.setViewMatrix(view)
        sessionManager.arObjectScene!!.setProjectionMatrix(projection)
        sessionManager.arObjectScene.setViewMatrix(view)
    }

    private fun extractMatrixFromCamera(frame: Frame): Pair<FloatArray, FloatArray> {
        val camera = frame.camera
        val projMatrix = FloatArray(16)
        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)
        val viewMatrix = FloatArray(16)
        camera.getViewMatrix(viewMatrix, 0)
        return Pair(projMatrix, viewMatrix)
    }

    private fun renderPointCloud(frame: Frame) {
        val pointCloud: PointCloud = frame.acquirePointCloud()
        sessionManager.mPointCloud!!.update(pointCloud)
        pointCloud.release()
    }

    fun getXY(x: Float, y: Float) {
        currentX = x
        currentY = y
    }

    private val textureId: Int
        get() = sessionManager.mCamera?.textureId ?: -1

}