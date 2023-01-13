#version 300 es
layout (location = 0) in vec4 aPosition;

uniform mat4 mvp;
uniform vec4 vColor;
uniform float vSize;
out vec4 uColor;

void main(){
    gl_Position = mvp * vec4(aPosition.xyx, 1.0);
    gl_PointSize = vSize;
    uColor = vColor;
}
