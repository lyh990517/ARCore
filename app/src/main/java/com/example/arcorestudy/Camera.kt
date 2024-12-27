package com.example.arcorestudy

import android.util.Log
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class Camera {
    var eye: Vec3 = Vec3(0, 0, -5)
    var currentMat = glm.translate(Mat4(), eye)
    fun touch(x: Float) {

    }

    fun getView(): Mat4 = glm.rotate(currentMat, 0f, Vec3(0, 1, 0))

}