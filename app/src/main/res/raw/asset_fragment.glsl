#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoord;

uniform sampler2D diffuse;
uniform sampler2D bump;

void main() {
    FragColor = mix(texture(diffuse,TexCoord),texture(bump,TexCoord),0.0) * vec4(1,1,1,1);
}

