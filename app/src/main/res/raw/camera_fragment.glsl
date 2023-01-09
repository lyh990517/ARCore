#version es 300
precision mediump float;

in vec2 TexCoord;

Uniform samplerExternalOES cameraTexture;
out vec4 FragColor;

void main(){
    FragColor = texture(cameraTexture,TexCoord);
}