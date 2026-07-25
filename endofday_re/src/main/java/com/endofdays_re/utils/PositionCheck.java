package com.endofdays_re.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.function.TriFunction;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class PositionCheck {
    private final TriFunction<Level, BlockPos, Player, Boolean> extraConditions;

    private PositionCheck(Builder builder, boolean defaultIfNone) {
        extraConditions = builder.extraConditions == null ? (level, blockPos, player) -> defaultIfNone : builder.extraConditions;
    }

    public static Builder create(BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner) {
        return new Builder(combiner);
    }

    @Nonnull
    public TriFunction<Level, BlockPos, Player, Boolean> getExtraConditions() {
        return extraConditions;
    }

    public static class Builder {
        private final BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner;
        private TriFunction<Level, BlockPos, Player, Boolean> extraConditions = null;

        public Builder(BiFunction<TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>, TriFunction<Level, BlockPos, Player, Boolean>> combiner) {
            this.combiner = combiner;
        }

        public Builder extraCondition(TriFunction<Level, BlockPos, Player, Boolean> extraCondition) {
            if (this.extraConditions == null) {
                this.extraConditions = extraCondition;
            } else {
                TriFunction<Level, BlockPos, Player, Boolean> oldCondition = this.extraConditions;
                this.extraConditions = combiner.apply(oldCondition, extraCondition);
            }
            return this;
        }

        public PositionCheck build(boolean defaultIfNone) {
            return new PositionCheck(this, defaultIfNone);
        }
    }

}
