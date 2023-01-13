#version 300 es
layout (location = 0) in vec4 aPosition;
uniform mat4 proj;
uniform mat4 view;
uniform vec4 uColor;
uniform float uPointSize;
out vec4 vColor;
void main() {
   vColor = uColor;
   gl_Position = proj * view * vec4(aPosition.xyz, 1.0);
   gl_PointSize = uPointSize;
}
