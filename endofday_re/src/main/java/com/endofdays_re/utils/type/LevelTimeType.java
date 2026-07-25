package com.endofdays_re.utils.type;


public enum LevelTimeType {
    DAY(990), NIGHT(12990), NONE(-1);

    public long value;

    LevelTimeType(int i) {
        this.value = i;
    }
}
