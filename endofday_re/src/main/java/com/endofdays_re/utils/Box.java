package com.endofdays_re.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

// 保持使用 record，它是存储坐标区间的绝佳选择
public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    // 修复：Record 的构造函数必须调用 canonical constructor (主构造函数)
    public Box(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * 修复报错：因为 record 是不可变的，expand 必须返回一个新的 Box 实例
     */
    public Box expand(int radius) {
        return new Box(
                this.minX - radius,
                this.minY - radius,
                this.minZ - radius,
                this.maxX + radius,
                this.maxY + radius,
                this.maxZ + radius
        );
    }

    /**
     * 转换为原版 AABB，用于对接 world.getEntities() 等系统方法，性能大幅提升
     */
    public AABB toAABB() {
        return new AABB(minX, minY, minZ, maxX + 1.0, maxY + 1.0, maxZ + 1.0);
    }

    public boolean isValid() {
        return minX < maxX && minY < maxY && minZ < maxZ;
    }

    public boolean in(BlockPos pos) {
        return in(pos, 0);
    }

    public boolean in(BlockPos pos, int maxOffset) {
        return pos.getX() >= minX - maxOffset && pos.getX() <= maxX + maxOffset &&
                pos.getY() >= minY - maxOffset && pos.getY() <= maxY + maxOffset &&
                pos.getZ() >= minZ - maxOffset && pos.getZ() <= maxZ + maxOffset;
    }

    public BlockPos randomPos(RandomSource random, @Nullable BlockPos groupCenterPos, int groupDistance) {
        if (groupCenterPos != null && groupDistance >= 0) {
            if (groupDistance == 0) return groupCenterPos;

            for (int i = 0; i < 100; i++) {
                // 使用 nextInt(range) 时，range 必须 > 0
                int dx = (groupDistance * 2 <= 0) ? 0 : random.nextInt(groupDistance * 2);
                int dy = (groupDistance * 2 <= 0) ? 0 : random.nextInt(groupDistance * 2);
                int dz = (groupDistance * 2 <= 0) ? 0 : random.nextInt(groupDistance * 2);

                BlockPos attempt = new BlockPos(
                        groupCenterPos.getX() - groupDistance + dx,
                        groupCenterPos.getY() - groupDistance + dy,
                        groupCenterPos.getZ() - groupDistance + dz);
                if (in(attempt, 2)) {
                    return attempt;
                }
            }
            return groupCenterPos;
        }

        // 确保 nextInt 的边界合法
        int rangeX = Math.max(1, maxX - minX + 1);
        int rangeY = Math.max(1, maxY - minY + 1);
        int rangeZ = Math.max(1, maxZ - minZ + 1);

        return new BlockPos(
                minX + random.nextInt(rangeX),
                minY + random.nextInt(rangeY),
                minZ + random.nextInt(rangeZ));
    }
}