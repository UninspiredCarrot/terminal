package com.terminal.buffer;

import com.terminal.model.CellAttributes;

import java.util.ArrayDeque;

public final class TerminalBuffer {

    private final int width;
    private final int height;
    private final int maxScrollback;
    private final TerminalLine[] screen;
    private final ArrayDeque<TerminalLine> scrollback;
    private int cursorRow;
    private int cursorCol;
    private CellAttributes currentAttributes;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0)       throw new IllegalArgumentException("width must be > 0, got: " + width);
        if (height <= 0)      throw new IllegalArgumentException("height must be > 0, got: " + height);
        if (maxScrollback <= 0) throw new IllegalArgumentException("maxScrollback must be > 0, got: " + maxScrollback);

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;
        this.screen = new TerminalLine[height];
        for (int i = 0; i < height; i++) {
            screen[i] = new TerminalLine(width);
        }
        this.scrollback = new ArrayDeque<>();
        this.cursorRow = 0;
        this.cursorCol = 0;
        this.currentAttributes = CellAttributes.DEFAULT;
    }

    TerminalLine[] getScreen()     { return screen; }

    public int getWidth()          { return width; }
    public int getHeight()         { return height; }
    public int getMaxScrollback()  { return maxScrollback; }
    public int getCursorRow()      { return cursorRow; }
    public int getCursorCol()      { return cursorCol; }
    public int getScrollbackSize() { return scrollback.size(); }

    public void setCursor(int row, int col) {
        if (row < 0 || row >= height) {
            throw new IllegalArgumentException(
                    "row " + row + " out of bounds for height " + height);
        }
        if (col < 0 || col >= width) {
            throw new IllegalArgumentException(
                    "col " + col + " out of bounds for width " + width);
        }
        this.cursorRow = row;
        this.cursorCol = col;
    }

    public void moveCursorUp(int n) {
        requireNonNegative(n, "n");
        cursorRow = Math.max(0, cursorRow - n);
    }

    public void moveCursorDown(int n) {
        requireNonNegative(n, "n");
        cursorRow = Math.min(height - 1, cursorRow + n);
    }

    public void moveCursorLeft(int n) {
        requireNonNegative(n, "n");
        cursorCol = Math.max(0, cursorCol - n);
    }

    public void moveCursorRight(int n) {
        requireNonNegative(n, "n");
        cursorCol = Math.min(width - 1, cursorCol + n);
    }

    public CellAttributes getCurrentAttributes() { return currentAttributes; }

    public void setCurrentAttributes(CellAttributes attributes) {
        if (attributes == null) throw new NullPointerException("attributes must not be null");
        this.currentAttributes = attributes;
    }

    private static void requireNonNegative(int n, String name) {
        if (n < 0) throw new IllegalArgumentException(name + " must be >= 0, got: " + n);
    }
}
