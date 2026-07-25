package com.endofdays_re.utils.type;

public enum Height {
    DOWN(8), UP(8), NONE(6);
    public final int limit;

    Height(int limit) {
        this.limit = limit;
    }
}
