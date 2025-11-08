#version 330 core

in vec2 TexCoord;
in float v_ShouldTint;

out vec4 FragColor;

uniform sampler2D textureSampler;
uniform vec3 u_TintColor;
uniform vec3 ambientLight;

void main() {
    vec4 texColor = texture(textureSampler, TexCoord);
    
    vec3 finalColor = texColor.rgb;
    if (v_ShouldTint > 0.5) {
        finalColor = texColor.rgb * u_TintColor;
    }
    
    finalColor = finalColor * ambientLight;
    
    FragColor = vec4(finalColor, texColor.a);
}

