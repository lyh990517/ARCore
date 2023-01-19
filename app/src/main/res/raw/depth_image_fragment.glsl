#version 300 es
layout(location = 0) in vec4 aPos;
layout(loaction = 1) in vec2 aTexCoord;

out vec2 TexCoord;

void main(){
    TexCoord = aTexCoord;
    gl_Position = aPos;
}