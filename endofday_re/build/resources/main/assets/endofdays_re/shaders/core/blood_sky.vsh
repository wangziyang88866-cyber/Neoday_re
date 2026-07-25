#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 texCoord0;
out vec3 viewPos;   // 相机空间方向：用于渐变/仰角计算（天生跟随视角，符合"越往上看越暗"的直觉）
out vec3 starDir;   // 世界空间（未旋转）方向：专门用于星星图案，保证星星"钉"在天空上不跟头转

void main() {
    // 关键修复 1（上一轮已修）：
    // Java 端在把顶点写入缓冲之前，已经用相机 view 矩阵变换过 Position，
    // 这里不能再乘一次 ModelViewMat，否则天空被转两次。
    gl_Position = ProjMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    viewPos = Position;

    // 关键修复 2（本轮新增）：
    // Java 端把"变换前"的本地方向（即天球未旋转时的方向，代表真实世界方位）
    // 编码进了 Color.rgb（-1~1 映射到 0~1）。这里解码出来，
    // 专门喂给片元着色器算星星的随机噪声。
    // 用它而不是 viewPos，是因为 viewPos 已经被相机旋转"污染"过——
    // 用 viewPos 算星星图案，等于把图案画死在屏幕坐标系里，
    // 转头时同一片星星永远对应同一个屏幕位置，看起来就像"星星跟着头转"。
    // 而 starDir 是世界固定方向，不管镜头怎么转，同一颗星星对应的世界方向不变，
    // 星星才会像真实天空一样"钉"在原地，只是相机在看它的哪一部分。
    starDir = Color.rgb * 2.0 - 1.0;
}
