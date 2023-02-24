package com.example.arcorestudy.rendering

import androidx.annotation.RawRes

data class FaceItem(
    val type: String,
    val objPath: String? = null,
    @RawRes val objId: Int? = null,
    val nosePath: String? = null,
    val rightEarPath: String? = null,
    val leftEarPath: String? = null,
    @RawRes val nose: Int? = null,
    @RawRes val rightEar: Int? = null,
    @RawRes val leftEar: Int? = null
)
