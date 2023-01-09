extension GL_OES_EGL_image_external : require
precision mediump float;

out vec4 FragColor;
in vec2 TexCoord;
uniform samplerExternalOES _surfaceTexture;

void main(){
    FragColor = texture2D(_surfaceTexture,TexCoord);
}