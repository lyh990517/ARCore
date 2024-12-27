package com.example.arcorestudy

import glm_.vec3.Vec3

data class Material(
    val ambientColor: Vec3,
    val diffuseColor: Vec3,
    val specularColor: Vec3,
    val diffuseTexture: Texture?,
    val specularTexture: Texture?
)
