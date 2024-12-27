package com.example.arcorestudy

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arcorestudy.databinding.ActivityMainBinding
import com.example.arcorestudy.rendering.FaceItem
import com.google.ar.core.Session

@Suppress("UNREACHABLE_CODE", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var renderingManager: RenderingManager
    private lateinit var renderer: MainRenderer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    fun switchCamera() = with(sessionManager) {
        // Set a camera configuration that usese the front-facing camera
        renderer.isFrontCamera = !renderer.isFrontCamera
        if (renderer.isFrontCamera) {
            pause()
            resume(setOf(Session.Feature.FRONT_CAMERA))
        } else {
            pause()
            resume(setOf())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        sessionManager = SessionManager.create(binding.glSurfaceView.context!!.applicationContext)
        renderingManager =
            RenderingManager.create(binding.glSurfaceView.context!!.applicationContext)
        sessionManager.create()

        renderer = MainRenderer(sessionManager, renderingManager)
        binding.glSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        binding.reset.setOnClickListener {
            renderingManager.cubeScene.clear()
            renderingManager.arObjectScene.clear()
            renderer.distanceLiveData.value = 0f
        }
        binding.faceType.isGone = true
        renderer.distanceLiveData.observe(this) {
            binding.distance.text = "$it m"
        }
        renderer.planeLiveData.observe(this) {
            binding.plane.text = it
        }
        renderer.mode.observe(this) {
            binding.change.text = it
        }
        renderer.faceType.observe(this) {
            binding.faceType.text = it
        }
        binding.change.setOnClickListener {
            if (renderer.mode.value == "arObject") {
                renderer.mode.value = "cube"
            } else {
                renderer.mode.value = "arObject"
            }
        }
        binding.pointCloud.setOnClickListener {
            if (binding.pointCloud.isChecked) {
                Log.e("123", "check")
                renderer.pointCloudLiveData.value = true
            } else {
                Log.e("123", "not check")
                renderer.pointCloudLiveData.value = false
            }
        }
        renderer.xLiveData.observe(this) {
            binding.xVal.text = it.toString()
        }
        renderer.yLiveData.observe(this) {
            binding.yVal.text = it.toString()
        }
        renderer.zLiveData.observe(this) {
            binding.zVal.text = it.toString()
        }
        var arr2 = arrayOf("faceObject", "faceFilter", "faceTips")
        var idx2 = 0
        binding.faceType.setOnClickListener {
            idx2++
            if (idx2 == 3) idx2 = 0
            renderer.faceType.value = arr2[idx2]
            renderingManager.selectFace(
                "faceObject",
                objPath = "PlagueMask_sketchfab.obj",
                objId = R.raw.plagmask
            )
        }
        var arr = arrayOf("near", "far", "all")
        var idx = 0
        binding.draw.setOnClickListener {
            idx++
            if (idx == 3) idx = 0
            renderer.drawingMode.value = arr[idx]
        }
        renderer.drawingMode.observe(this) {
            binding.draw.text = it
        }
        binding.cameraMode.setOnClickListener {
            switchCamera()
            binding.reset.isGone = !binding.reset.isGone
            binding.draw.isGone = !binding.draw.isGone
            binding.change.isGone = !binding.change.isGone
            binding.x.isGone = !binding.x.isGone
            binding.y.isGone = !binding.y.isGone
            binding.z.isGone = !binding.z.isGone
            binding.xVal.isGone = !binding.xVal.isGone
            binding.yVal.isGone = !binding.yVal.isGone
            binding.zVal.isGone = !binding.zVal.isGone
            binding.distance.isGone = !binding.distance.isGone
            binding.faceType.isGone = !binding.faceType.isGone
            binding.r.text = "x"
            binding.g.text = "y"
            binding.b.text = "z"
            binding.red.progress = 50
            binding.blue.progress = 50
            binding.green.progress = 50
            binding.size.progress = 50
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onDestroy()
    }

    override fun onPause() = with(sessionManager) {
        super.onPause()
        Log.e("activity", "onPause")
        binding.glSurfaceView.onPause()
        pause()
    }

    override fun onResume() = with(sessionManager) {
        super.onResume()
        Log.e("activity", "onResume")
        requestCameraPermission()
        resume(setOf())
        binding.glSurfaceView.onResume()
    }

    override fun onDestroy() = with(sessionManager) {
        Log.e("activity", "onDestroy")
        destroy()
        super.onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.onTouch = true
                renderer.getXY(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                renderer.getXY(event.x, event.y)
            }

            MotionEvent.ACTION_UP -> {
                renderer.onTouch = false
            }
        }
        return true
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}