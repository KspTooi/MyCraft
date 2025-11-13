#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform vec2 screenSize;
uniform vec2 position;
uniform vec2 size;
uniform vec4 color;
uniform vec2 u_TexCoordOffset;
uniform vec2 u_TexCoordScale;

out vec4 fragColor;
out vec2 vs_TexCoord;

void main() {
    vec2 normalizedPos = (aPos * size + position) / screenSize * 2.0 - 1.0;
    normalizedPos.y = -normalizedPos.y;
    gl_Position = vec4(normalizedPos, 0.0, 1.0);
    fragColor = color;
    vs_TexCoord = u_TexCoordOffset + aTexCoord * u_TexCoordScale;
}

