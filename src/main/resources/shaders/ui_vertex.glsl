#version 330 core

layout (location = 0) in vec2 aPos;

uniform vec2 screenSize;
uniform vec2 position;
uniform vec2 size;
uniform vec3 color;

out vec3 fragColor;

void main() {
    vec2 normalizedPos = (aPos * size + position) / screenSize * 2.0 - 1.0;
    normalizedPos.y = -normalizedPos.y;
    gl_Position = vec4(normalizedPos, 0.0, 1.0);
    fragColor = color;
}

