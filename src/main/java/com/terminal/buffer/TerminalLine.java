package com.terminal.buffer;

import com.terminal.model.Cell;

public final class TerminalLine {

    private final Cell[] cells;

    public TerminalLine(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0, got: " + width);
        }
        this.cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            cells[i] = new Cell();
        }
    }

    public int getWidth() {
        return cells.length;
    }

    public Cell getCell(int column) {
        checkBounds(column);
        return cells[column];
    }

    public void setCell(int column, Cell cell) {
        checkBounds(column);
        if (cell == null) {
            throw new NullPointerException("cell must not be null");
        }
        cells[column] = cell;
    }

    private void checkBounds(int column) {
        if (column < 0 || column >= cells.length) {
            throw new IndexOutOfBoundsException(
                    "column " + column + " out of bounds for width " + cells.length);
        }
    }
}
