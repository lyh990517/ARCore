package com.example.arcorestudy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityMainBinding

@Suppress("UNREACHABLE_CODE", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var renderer: MainRenderer
    private var r = 0f
    private var g = 0f
    private var b = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        sessionManager = SessionManager.create(binding.glSurfaceView.context!!.applicationContext)
        sessionManager.create()
        renderer = MainRenderer(sessionManager)
        binding.glSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        binding.reset.setOnClickListener {
            sessionManager.cubeScene.clear()
            sessionManager.arObjectScene.clear()
            renderer.distanceLiveData.value = 0f
        }
        renderer.distanceLiveData.observe(this) {
            binding.distance.text = "$it m"
        }
        renderer.planeLiveData.observe(this) {
            binding.plane.text = it
        }
        renderer.mode.observe(this) {
            binding.change.text = it
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
        binding.red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                r = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                r = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                g = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                g = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                b = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                b = seekBar?.progress?.toFloat()?.times(0.01f) ?: 0f
                renderer.getRGB(r, g, b)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Log.e("activity", "onPause")
        binding.glSurfaceView.onPause()
        sessionManager.mSession?.pause()
    }

    override fun onResume() {
        super.onResume()
        Log.e("activity", "onResume")
        requestCameraPermission()
        sessionManager.resume()
        binding.glSurfaceView.onResume()
    }

    override fun onDestroy() {
        Log.e("activity", "onDestroy")
        sessionManager.destroy()
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