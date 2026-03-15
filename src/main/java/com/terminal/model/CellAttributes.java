package com.terminal.model;

import java.util.Objects;

public final class CellAttributes {

    public static final CellAttributes DEFAULT =
            new CellAttributes(Color.DEFAULT, Color.DEFAULT, 0);

    private final Color foreground;
    private final Color background;
    private final int styles;

    public CellAttributes(Color foreground, Color background, int styles) {
        this.foreground = Objects.requireNonNull(foreground, "foreground must not be null");
        this.background = Objects.requireNonNull(background, "background must not be null");
        this.styles = styles;
    }

    public Color foreground() { return foreground; }
    public Color background() { return background; }
    public int styles()       { return styles; }

    public boolean hasStyle(Style s) {
        return (styles & s.bit()) != 0;
    }

    public CellAttributes withForeground(Color foreground) {
        return new CellAttributes(foreground, background, styles);
    }

    public CellAttributes withBackground(Color background) {
        return new CellAttributes(foreground, background, styles);
    }

    public CellAttributes withStyles(int styles) {
        return new CellAttributes(foreground, background, styles);
    }

    public CellAttributes withStyle(Style s) {
        return new CellAttributes(foreground, background, styles | s.bit());
    }

    public CellAttributes withoutStyle(Style s) {
        return new CellAttributes(foreground, background, styles & ~s.bit());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellAttributes other)) return false;
        return styles == other.styles
                && foreground == other.foreground
                && background == other.background;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }

    @Override
    public String toString() {
        return "CellAttributes{foreground=" + foreground
                + ", background=" + background
                + ", styles=" + styles + "}";
    }
}
