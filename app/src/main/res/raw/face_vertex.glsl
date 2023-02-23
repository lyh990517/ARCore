attribute vec3 aPos;
attribute vec2 aTexCoord;
attribute vec3 aNormal;

uniform mat4 mvp;
uniform mat4 mv;

varying vec2 TexCoord;
varying vec3 v_ViewNormal;

void main() {
    gl_Position = mvp * vec4(aPos, 1.0);

    TexCoord = aTexCoord;
    v_ViewNormal = normalize((mv * vec4(aNormal, 0.0)).xyz);
}
