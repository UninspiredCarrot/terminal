package com.terminal.model;

public enum Style {
    BOLD(1),
    ITALIC(2),
    UNDERLINE(4);

    private final int bit;

    Style(int bit) {
        this.bit = bit;
    }

    public int bit() {
        return bit;
    }
}
