package com.example.arcorestudy

abstract class Scene {
    protected val timer = Timer()
    val camera = Camera()
    abstract fun init(width: Int, height: Int)
    abstract fun draw()
}