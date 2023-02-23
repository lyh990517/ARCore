#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 mvp;
uniform mat4 mv;

out vec2 TexCoord;
out vec3 v_ViewPosition;
out vec3 v_ViewNormal;

void main() {
    gl_Position = mvp * vec4(aPos, 1.0);

    TexCoord = aTexCoord;
    v_ViewPosition = (mv * aPos).xyz;
    v_ViewNormal = normalize((mv * vec4(aNormal, 0.0)).xyz);
}
