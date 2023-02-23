package com.example.arcorestudy

import android.opengl.GLSurfaceView
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.arcorestudy.rendering.Face.FaceFilterRendering
import com.example.arcorestudy.rendering.Face.FaceRendering
import com.google.ar.core.*
import com.google.ar.core.exceptions.SessionPausedException
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
    var isFrontCamera = false
    val faceType = MutableLiveData("faceObject")
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) = with(sessionManager) {
        mCamera.init()
        mPointCloud.init()
        cubeScene.init()
        arObjectScene.init()
        noseRendering.init()
        rightEarRendering.init()
        leftEarRendering.init()
        faceFilterRendering.init()
        faceFilterRendering.initMesh()
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

    private fun render() = with(sessionManager) {
        glDepthMask(false)
        mCamera.draw()
        glDepthMask(true)
        if (pointCloudLiveData.value == true) {
            mPointCloud.draw()
        }
        cubeScene.draw()
        arObjectScene.draw()
        if (isFrontCamera) {
            when(faceType.value){
                "faceMask" -> {
                    faceFilterRendering.draw()
                }
                "faceObject" -> {
                    faceFilterRendering.drawMesh()
                }
                "faceTips" -> {
                    noseRendering.draw()
                    leftEarRendering.draw()
                    rightEarRendering.draw()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun preRender() = with(sessionManager) {
        updateSession(mViewportWidth, mViewportHeight)
        mSession?.setCameraTextureName(textureId)
        try {
            val frame = mSession!!.update()
            if (frame.hasDisplayGeometryChanged()) {
                mCamera.transformDisplayGeometry(frame)
            }
            renderPointCloud(frame)
            extractMatrixFromCamera(frame).let { setMatrix(it.first, it.second) }
            getHitPose(frame)
            if (isFrontCamera) {
                detectFace()
            } else {
                detectPlane()
            }
        } catch (_: SessionPausedException) {

        }
    }

    private fun detectFace() = with(sessionManager) {
        val faces =
            mSession?.getAllTrackables(com.google.ar.core.AugmentedFace::class.java)
        faces?.forEach { face ->
            if (face.trackingState == TrackingState.TRACKING) {
                rightEarRendering.setPose(face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT))
                leftEarRendering.setPose(face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT))
                noseRendering.setPose(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP))
                faceFilterRendering.setFace(
                    face.meshVertices,
                    face.meshTriangleIndices,
                    face.meshTextureCoordinates,
                    face.meshNormals,
                    face.centerPose
                )
            }
        }
        if (faces.isNullOrEmpty()) {
            planeLiveData.postValue("nothing detected...")
        } else {
            planeLiveData.postValue("face is detected!!")
        }
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
            planeLiveData.postValue("nothing detected...")
        }
    }

    private fun getHitPose(frame: Frame) {
        val results = frame.hitTest(currentX, currentY).filter {
            val trackable = it.trackable
            trackable is DepthPoint
        }
        results.forEach {
            val distance = it.distance
            distanceLiveData.postValue(distance)
            val pose = it.hitPose
            addPoint(Vec3(pose.tx(), pose.ty(), pose.tz()))
            xLiveData.postValue(pose.tx())
            yLiveData.postValue(pose.ty())
            zLiveData.postValue(pose.tz())
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
        noseRendering.setProjectionMatrix(projection)
        noseRendering.setViewMatrix(view)
        rightEarRendering.setProjectionMatrix(projection)
        rightEarRendering.setViewMatrix(view)
        leftEarRendering.setProjectionMatrix(projection)
        leftEarRendering.setViewMatrix(view)
        faceFilterRendering.setProjectionMatrix(projection)
        faceFilterRendering.setViewMatrix(view)
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

    fun getXYZ(x: Float, y: Float, z: Float) {
        sessionManager.faceFilterRendering.getXYZ(x, y, z)
    }

    fun setSize(size: Float) {
        sessionManager.cubeScene.size = size
        sessionManager.faceFilterRendering.setSize(size)
    }

    private val textureId: Int
        get() = sessionManager.mCamera.textureId ?: -1

}