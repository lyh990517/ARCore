#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
uniform samplerExternalOES sTexture;
in vec2 vTexCoord;
out vec4 FragColor;
void main() {
    FragColor = texture(sTexture, vTexCoord);
}