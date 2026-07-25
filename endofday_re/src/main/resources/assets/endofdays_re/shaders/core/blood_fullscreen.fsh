#version 150

uniform float intensity; // 0.0 ~ 1.0

out vec4 fragColor;

void main() {
    float r = (0.6 + 0.3 * intensity) * 0.15;
    float g = (0.08 - 0.05 * intensity) * 0.15;
    float b = (0.04 - 0.03 * intensity) * 0.15;
    // 透明度略微提高（更不透明）
    float a = 0.5 + 0.1 * intensity; // 范围 0.25 ~ 0.80

    fragColor = vec4(r, g, b, a);
}