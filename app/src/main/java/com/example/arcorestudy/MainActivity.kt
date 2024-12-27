package com.example.arcorestudy

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var renderer: MainRenderer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        sessionManager = SessionManager.create(binding.glSurfaceView.context!!.applicationContext)
        sessionManager.create()

        renderer = MainRenderer(context = this, sessionManager = sessionManager)
        binding.glSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        binding.reset.setOnClickListener {
            renderer.arObjectScene.clear()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
        sessionManager.pause()
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()
        sessionManager.resume(setOf())
        binding.glSurfaceView.onResume()
    }

    override fun onDestroy() {
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
}