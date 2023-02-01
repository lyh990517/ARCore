package com.example.arcorestudy

import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.ar.core.*
import glm_.vec3.Vec3
import javax.microedition.khronos.egl.EGLConfig

class MainRenderer(private val sessionManager: SessionManager) :
    GLSurfaceView.Renderer {
    private var mViewportWidth = 0
    private var mViewportHeight = 0
    private var currentX = 0f
    private var currentY = 0f
    var onTouch = false
    val mode = MutableLiveData("cube")
    val drawingMode = MutableLiveData("near")
    val distanceLiveData = MutableLiveData<Float>()
    val planeLiveData = MutableLiveData<String>()
    val pointCloudLiveData = MutableLiveData(false)
    val xLiveData = MutableLiveData<Float>()
    val yLiveData = MutableLiveData<Float>()
    val zLiveData = MutableLiveData<Float>()

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {}

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) = with(sessionManager) {
        glViewport(0, 0, width, height)
        mCamera.init()
        mPointCloud.init()
        cubeScene.init()
        arObjectScene.init()
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

    private fun render() = with(sessionManager) {
        glDepthMask(false)
        mCamera.draw()
        glDepthMask(true)
        if (pointCloudLiveData.value == true) {
            mPointCloud.draw()
        }
        cubeScene.draw()
        arObjectScene.draw()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() = with(sessionManager) {
        updateSession(mViewportWidth, mViewportHeight)
        mSession?.setCameraTextureName(textureId)
        val frame = mSession!!.update()
        if (frame.hasDisplayGeometryChanged()) {
            mCamera.transformDisplayGeometry(frame)
        }
        renderPointCloud(frame)
        extractMatrixFromCamera(frame).let { setMatrix(it.first, it.second) }
        getHitPose(frame)
        detectPlane()
    }

    private fun detectPlane() = with(sessionManager) {
        var isPlane = false
        val planes = mSession!!.getAllTrackables(Plane::class.java)
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
        val results = frame.hitTest(currentX, currentY).filter {
            val trackable = it.trackable
            trackable is DepthPoint
        }
        when (drawingMode.value) {
            "near" -> {
                if (results.size > 0) {
                    val distance = results[0].distance
                    distanceLiveData.postValue(distance)
                    val pose = results[0].hitPose
                    addPoint(Vec3(pose.tx(), pose.ty(), pose.tz()))
                    xLiveData.postValue(pose.tx())
                    yLiveData.postValue(pose.ty())
                    zLiveData.postValue(pose.tz())
                }
            }
            "far" -> {
                if (results.size > 0) {
                    val distance = results[results.size - 1].distance
                    distanceLiveData.postValue(distance)
                    val pose = results[results.size - 1].hitPose
                    addPoint(Vec3(pose.tx(), pose.ty(), pose.tz()))
                    xLiveData.postValue(pose.tx())
                    yLiveData.postValue(pose.ty())
                    zLiveData.postValue(pose.tz())
                }
            }
            "all" -> {
                results.forEach { hitResult ->
                    val distance = hitResult.distance
                    distanceLiveData.postValue(distance)
                    val pose = hitResult.hitPose
                    addPoint(Vec3(pose.tx(), pose.ty(), pose.tz()))
                    xLiveData.postValue(pose.tx())
                    yLiveData.postValue(pose.ty())
                    zLiveData.postValue(pose.tz())
                }
            }
        }
    }

    private fun addPoint(vec3: Vec3) = with(sessionManager) {
        if (onTouch) {
            when (mode.value) {
                "cube" -> {
                    cubeScene.addPosition(vec3)
                }
                "arObject" -> {
                    arObjectScene.addPosition(vec3)
                }
            }
        }
    }

    private fun setMatrix(projection: FloatArray, view: FloatArray) = with(sessionManager) {
        mPointCloud.setProjectionMatrix(projection)
        mPointCloud.setViewMatrix(view)
        cubeScene.setProjectionMatrix(projection)
        cubeScene.setViewMatrix(view)
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

    private fun renderPointCloud(frame: Frame) {
        val pointCloud: PointCloud = frame.acquirePointCloud()
        sessionManager.mPointCloud.update(pointCloud)
        pointCloud.release()
    }

    fun getXY(x: Float, y: Float) {
        currentX = x
        currentY = y
    }

    fun getRGB(red: Float, green: Float, blue: Float) {
        sessionManager.cubeScene.cubeRGB(red, green, blue)
    }
    fun setSize(size: Float){
        sessionManager.cubeScene.size = size
    }

    private val textureId: Int
        get() = sessionManager.mCamera.textureId ?: -1

}