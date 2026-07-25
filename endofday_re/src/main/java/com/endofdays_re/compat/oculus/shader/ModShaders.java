package com.endofdays_re.compat.oculus.shader;

import net.minecraft.client.renderer.ShaderInstance;

import javax.annotation.Nullable;

public class ModShaders {
    private static @Nullable ShaderInstance bloodSkyShader;
    private static @Nullable ShaderInstance bloodFullscreenShader;

    public static @Nullable ShaderInstance getBloodSkyShader() {
        return bloodSkyShader;
    }

    public static void setBloodSkyShader(ShaderInstance shader) {
        bloodSkyShader = shader;
    }

    public static @Nullable ShaderInstance getBloodFullscreenShader() {
        return bloodFullscreenShader;
    }

    public static void setBloodFullscreenShader(ShaderInstance shader) {
        bloodFullscreenShader = shader;
    }
}