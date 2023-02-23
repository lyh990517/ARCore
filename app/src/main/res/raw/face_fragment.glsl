#version 300 es
precision mediump float;

in vec2 TexCoord;
uniform sampler2D texture1;

uniform vec4 u_LightingParameters;
uniform vec4 u_MaterialParameters;
uniform vec4 u_ColorCorrectionParameters;

in vec3 v_ViewPosition;
uniform vec3 v_ViewNormal;
in vec2 v_TexCoord;
uniform vec4 u_ObjColor;

out vec4 FragColor;
void main() {
    const float kGamma = 0.4545454;
    const float kInverseGamma = 2.2;
    const float kMiddleGrayGamma = 0.466;
    const float kMToMm = 1000.0;
    vec3 viewLightDirection = u_LightingParameters.xyz;
    vec3 colorShift = u_ColorCorrectionParameters.rgb;
    float averagePixelIntensity = u_ColorCorrectionParameters.a;
    float materialAmbient = u_MaterialParameters.x;
    float materialDiffuse = u_MaterialParameters.y;
    float materialSpecular = u_MaterialParameters.z;
    float materialSpecularPower = u_MaterialParameters.w;
    vec3 viewFragmentDirection = normalize(v_ViewPosition);
    vec3 viewNormal = normalize(v_ViewNormal);
    vec4 objectColor = texture(texture1, vec2(TexCoord.x, 1.0 - TexCoord.y));
    FragColor = texture(texture1,TexCoord);

    if (u_ObjColor.a >= 255.0) {
       float intensity = objectColor.r;
       objectColor.rgb = u_ObjColor.rgb * intensity / 255.0;
    }
     objectColor.rgb = pow(objectColor.rgb, vec3(kInverseGamma));
     float ambient = materialAmbient;
     float diffuse = materialDiffuse * 0.5 * (dot(viewNormal, viewLightDirection) + 1.0);
     vec3 reflectedLightDirection = reflect(viewLightDirection, viewNormal);
     float specularStrength = max(0.0, dot(viewFragmentDirection,reflectedLightDirection));
     float specular = materialSpecular * pow(specularStrength, materialSpecularPower);
     vec3 color = objectColor.rgb * (ambient + diffuse) + specular;

     color.rgb = pow(color, vec3(kGamma));
     color *= colorShift * (averagePixelIntensity / kMiddleGrayGamma);

     FragColor.rgb = color;
     FragColor.a = objectColor.a;

}