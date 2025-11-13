#version 330 core

in vec4 fragColor;
in vec2 vs_TexCoord;

uniform bool u_UseTexture;
uniform sampler2D u_Texture;

out vec4 FragColor;

void main() {
    if (u_UseTexture) {
        vec4 sampledColor = texture(u_Texture, vs_TexCoord);
        float alpha = sampledColor.r;
        if (alpha < 0.1) {
            discard;
        }
        FragColor = vec4(fragColor.rgb, fragColor.a * alpha);
    } else {
        FragColor = fragColor;
    }
}

