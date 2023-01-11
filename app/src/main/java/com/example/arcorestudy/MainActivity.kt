package com.example.arcorestudy

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityMainBinding

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        sessionManager = SessionManager(binding.glSurfaceView.context!!.applicationContext)
        (getSystemService(DISPLAY_SERVICE) as DisplayManager).registerDisplayListener(
            sessionManager.displayListener,
            null
        )
        binding.glSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(2)
            setRenderer(MainRenderer(this@MainActivity, sessionManager))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
        sessionManager.mSession?.pause()
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()
        sessionManager.resume(this)
        binding.glSurfaceView.onResume()
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
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}