package com.endofdays_re.utils.tools;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 优化后的纹理管理器 - 使用动态图集减少 Draw Call
 */
public class TextureLoader {
    // 图集配置
    private static final int ATLAS_SIZE = 2048; // 总图集大小
    private static final int PADDING = 2;       // 防止像素溢出的间距
    private static final ResourceLocation ATLAS_LOCATION = ResourceLocation.fromNamespaceAndPath("external", "main_atlas");
    // 存储已加载纹理的 UV 信息
    private static final Map<String, SpriteInfo> LOADED_SPRITES = new HashMap<>();
    private static NativeImage atlasImage;
    private static DynamicTexture atlasTexture;
    // 排列算法变量
    private static int cursorX = 0;
    private static int cursorY = 0;
    private static int rowMaxHeight = 0;

    static {
        initAtlas();
    }

    private static void initAtlas() {
        atlasImage = new NativeImage(ATLAS_SIZE, ATLAS_SIZE, true);
        atlasTexture = new DynamicTexture(atlasImage);
        Minecraft.getInstance().getTextureManager().register(ATLAS_LOCATION, atlasTexture);
    }

    /**
     * 核心加载方法：将外部文件塞进图集
     */
    public static SpriteInfo load(File file, String textureId) {
        if (LOADED_SPRITES.containsKey(textureId)) {
            return LOADED_SPRITES.get(textureId);
        }

        if (!file.exists()) return null;

        try (FileInputStream is = new FileInputStream(file)) {
            NativeImage spriteImg = NativeImage.read(is);
            int w = spriteImg.getWidth();
            int h = spriteImg.getHeight();

            // 简单的行排列算法 (Shelf Packing)
            if (cursorX + w + PADDING > ATLAS_SIZE) {
                cursorX = 0;
                cursorY += rowMaxHeight + PADDING;
                rowMaxHeight = 0;
            }

            if (cursorY + h + PADDING > ATLAS_SIZE) {
                System.err.println("错误：动态图集已满！无法加载: " + textureId);
                spriteImg.close();
                return null;
            }

            // 将小图拷贝到大图
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    atlasImage.setPixelRGBA(cursorX + x, cursorY + y, spriteImg.getPixelRGBA(x, y));
                }
            }

            // 记录 UV 信息
            SpriteInfo info = new SpriteInfo(
                    (float) cursorX / ATLAS_SIZE,
                    (float) cursorY / ATLAS_SIZE,
                    (float) (cursorX + w) / ATLAS_SIZE,
                    (float) (cursorY + h) / ATLAS_SIZE,
                    w, h
            );

            // 更新指针
            cursorX += w + PADDING;
            rowMaxHeight = Math.max(rowMaxHeight, h);

            // 更新显存
            atlasTexture.upload();
            LOADED_SPRITES.put(textureId, info);
            spriteImg.close();
            return info;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 版本 A：通过 ID 渲染（内部自动查找缓存）
     */
    public static void render(GuiGraphics graphics, String textureId, int x, int y, int width, int height) {
        SpriteInfo sprite = LOADED_SPRITES.get(textureId);
        if (sprite != null) {
            render(graphics, sprite, x, y, width, height);
        }
    }

    /**
     * 版本 B：通过 SpriteInfo 直接渲染（性能更高，drawBackdrop 建议用这个）
     */
    public static void render(GuiGraphics graphics, SpriteInfo sprite, int x, int y, int width, int height) {
        if (sprite == null) return;

        // 参数解释：
        // ATLAS_LOCATION: 大图集的 ResourceLocation ("external:main_atlas")
        // x, y: 屏幕上的目标坐标
        // width, height: 要画多大 (拉伸后的尺寸)
        // sprite.u0 * ATLAS_SIZE, sprite.v0 * ATLAS_SIZE: 在大图中从哪个像素坐标开始切
        // sprite.width, sprite.height: 在大图中切多少像素出来
        // ATLAS_SIZE, ATLAS_SIZE: 整个大图集的总像素大小 (2048)
        graphics.blit(ATLAS_LOCATION,
                x, y,
                width, height,
                sprite.u0 * ATLAS_SIZE,
                sprite.v0 * ATLAS_SIZE,
                sprite.width,
                sprite.height,
                ATLAS_SIZE,
                ATLAS_SIZE);
    }

    /**
     * 清理资源
     */
    public static void cleanup() {
        atlasTexture.close();
        atlasImage.close();
        LOADED_SPRITES.clear();
        initAtlas(); // 重新初始化
    }

    /**
     * 存储贴图在图集中的位置
     */
    public record SpriteInfo(float u0, float v0, float u1, float v1, int width, int height) {
    }
}