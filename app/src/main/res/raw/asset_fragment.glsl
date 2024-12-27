#version 300 es
precision mediump float;

in vec2 TexCoord; // 버텍스 쉐이더로부터 전달된 텍스처 좌표
out vec4 FragColor; // 최종 픽셀 색상

uniform sampler2D diffuse; // 디퓨즈 텍스처
uniform sampler2D bump;     // 범프(노멀) 텍스처

void main()
{
    vec4 diffuseColor = texture(diffuse, TexCoord); // 디퓨즈 텍스처 샘플링
    vec4 bumpColor = texture(bump, TexCoord);       // 범프 텍스처 샘플링

    // 단순히 두 텍스처를 혼합 (여기서는 50% 비율로 혼합)
    FragColor = mix(diffuseColor, bumpColor, 0.5);
}
