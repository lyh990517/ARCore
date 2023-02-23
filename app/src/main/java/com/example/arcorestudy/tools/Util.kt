package com.example.arcorestudy.tools

import glm_.mat4x4.Mat4

fun Mat4.toFloatArray(): FloatArray{
    val floatArray = FloatArray(16)
    floatArray[0] = this.a0
    floatArray[1] = this.a1
    floatArray[2] = this.a2
    floatArray[3] = this.a3
    floatArray[4] = this.b0
    floatArray[5] = this.b1
    floatArray[6] = this.b2
    floatArray[7] = this.b3
    floatArray[8] = this.c0
    floatArray[9] = this.c1
    floatArray[10] = this.c2
    floatArray[11] = this.c3
    floatArray[12] = this.d0
    floatArray[13] = this.d1
    floatArray[14] = this.d2
    floatArray[15] = this.d3
    return floatArray
}