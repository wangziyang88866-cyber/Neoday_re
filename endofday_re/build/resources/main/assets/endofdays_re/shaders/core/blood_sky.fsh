#version 150

uniform float bloodIntensity;
uniform float GameTime;

in vec2 texCoord0;
in vec3 viewPos;
in vec3 starDir;

out vec4 fragColor;

float hash(vec3 p)
{
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

void main()
{
    // 使用世界方向，不再使用 viewPos
    vec3 dir = normalize(starDir);

    // 世界高度
    float height = clamp(dir.y, -0.08, 1.0);

    //---------------------------------------
    // 天空渐变
    //---------------------------------------

    float wHorizon = smoothstep(-0.08, 0.15, height);
    float wZenith  = smoothstep(0.15, 0.90, height);

    vec3 horizonColor = mix(
            vec3(0.020, 0.006, 0.006),
            vec3(0.550, 0.090, 0.040),
            bloodIntensity
    );

    vec3 midColor = mix(
            vec3(0.012, 0.004, 0.004),
            vec3(0.240, 0.035, 0.020),
            bloodIntensity
    );

    vec3 zenithColor = mix(
            vec3(0.000, 0.000, 0.000),
            vec3(0.050, 0.008, 0.010),
            bloodIntensity
    );

    vec3 skyColor = mix(horizonColor, midColor, wHorizon);
    skyColor = mix(skyColor, zenithColor, wZenith);

    //---------------------------------------
    // 地平线辉光
    //---------------------------------------

    float glowBand =
    (1.0 - smoothstep(0.0, 0.22, max(height, 0.0)))
    * bloodIntensity;

    vec3 glowColor =
    vec3(0.65, 0.07, 0.02)
    * glowBand
    * 0.55;

    //---------------------------------------
    // 星星
    //---------------------------------------

    vec3 starColor = vec3(0.0);

    vec3 sDir = normalize(starDir);

    vec3 cell = floor(sDir * 220.0);

    float starRand = hash(cell);

    if (starRand > 0.975 && height > 0.20)
    {
        float twinkle =
        0.5 +
        0.5 *
        sin(GameTime * 6.2831853 * 40.0 + starRand * 60.0);

        float brightness =
        (starRand * 0.5 + 0.25) *
        (0.35 + 0.65 * twinkle);

        starColor = vec3(
                brightness * 0.85,
                brightness * (0.22 + 0.35 * bloodIntensity),
                brightness * 0.15
        );

        starColor *= mix(1.0, 0.5, bloodIntensity);
    }

    //---------------------------------------
    // 合成
    //---------------------------------------

    vec3 finalColor =
    skyColor +
    glowColor +
    starColor;

    fragColor = vec4(finalColor, 1.0);
}