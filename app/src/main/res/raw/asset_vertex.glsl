#version 300 es
layout(location = 0) in vec3 aPos;      // 위치 속성
layout(location = 1) in vec2 aTexCoord; // 텍스처 좌표 속성

uniform mat4 mvp; // 모델-뷰-프로젝션 행렬

out vec2 TexCoord; // 프래그먼트 쉐이더로 전달할 텍스처 좌표

void main()
{
    gl_Position = mvp * vec4(aPos, 1.0); // 위치 변환
    TexCoord = aTexCoord;                 // 텍스처 좌표 전달
}
