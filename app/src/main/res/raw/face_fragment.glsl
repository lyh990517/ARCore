#version 300 es
precision mediump float;

in vec2 TexCoord;
in float v_Depth;
uniform sampler2D texture1;
uniform sampler2D depthTexture;
out vec4 FragColor;
void main() {
// 현재 픽셀의 깊이값을 읽어옵니다.
    float currentDepth = v_Depth;
    // depth texture에서 깊이값을 읽어옵니다.
    float depthValue = texture(depthTexture, TexCoord).r;
    if (currentDepth < depthValue) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0); // Occlusion 처리
    } else {
        FragColor = texture(texture1, TexCoord); // 일반 텍스쳐 처리
    }
}