package com.terminal.buffer;

import com.terminal.model.Cell;
import com.terminal.model.CellAttributes;

import java.util.ArrayDeque;
import java.util.Arrays;

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
        if (maxScrollback < 0) throw new IllegalArgumentException("maxScrollback must be >= 0, got: " + maxScrollback);

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
    TerminalLine[] getScrollbackLines() { return scrollback.toArray(new TerminalLine[0]); }

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

    public void fillLine(int row, char c, CellAttributes attributes) {
        if (row < 0 || row >= height) {
            throw new IllegalArgumentException(
                    "row " + row + " out of bounds for height " + height);
        }
        if (attributes == null) throw new NullPointerException("attributes must not be null");
        TerminalLine line = screen[row];
        for (int col = 0; col < width; col++) {
            line.setCell(col, new Cell(c, attributes));
        }
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen[i] = new TerminalLine(width);
        }
        cursorRow = 0;
        cursorCol = 0;
    }

    public void clearAll() {
        clearScreen();
        scrollback.clear();
    }

    public void insertLineAtBottom() {
        scrollUp();
    }

    public void write(String text) {
        if (text == null || text.isEmpty()) return;
        for (int i = 0; i < text.length(); i++) {
            screen[cursorRow].setCell(cursorCol,
                    new Cell(text.charAt(i), currentAttributes));
            cursorCol++;
            if (cursorCol >= width) {
                cursorCol = 0;
                cursorRow++;
                if (cursorRow >= height) {
                    scrollUp();
                    cursorRow = height - 1;
                }
            }
        }
    }

    public CellAttributes getCurrentAttributes() { return currentAttributes; }

    public void setCurrentAttributes(CellAttributes attributes) {
        if (attributes == null) throw new NullPointerException("attributes must not be null");
        this.currentAttributes = attributes;
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) return;

        Cell[] newCells = new Cell[text.length()];
        for (int i = 0; i < text.length(); i++) {
            newCells[i] = new Cell(text.charAt(i), currentAttributes);
        }

        insertCellsAt(cursorRow, cursorCol, newCells);

        // Advance cursor — same rule as write
        for (int i = 0; i < text.length(); i++) {
            cursorCol++;
            if (cursorCol >= width) {
                cursorCol = 0;
                cursorRow++;
                if (cursorRow >= height) {
                    scrollUp();
                    cursorRow = height - 1;
                }
            }
        }
    }

    private void insertCellsAt(int row, int col, Cell[] cells) {
        if (cells.length == 0) return;

        TerminalLine line = screen[row];
        int slotsAvailable = width - col;

        // Save the existing tail (col to end-of-line), copying each cell
        Cell[] tail = new Cell[slotsAvailable];
        for (int i = 0; i < slotsAvailable; i++) {
            Cell src = line.getCell(col + i);
            tail[i] = new Cell(src.character(), src.attributes());
        }

        // Place inserted cells (at most slotsAvailable)
        int placed = Math.min(cells.length, slotsAvailable);
        for (int i = 0; i < placed; i++) {
            line.setCell(col + i, cells[i]);
        }

        // Place as much tail as fits after the inserted cells
        int tailStart = col + placed;               // = col + placed
        int tailFits  = width - tailStart;          // = slotsAvailable - placed
        for (int i = 0; i < tailFits; i++) {
            line.setCell(tailStart + i, tail[i]);
        }

        // Compute spill — the cells that were pushed past end-of-line
        Cell[] spill;
        if (cells.length <= slotsAvailable) {
            // Last `cells.length` elements of tail no longer fit
            spill = Arrays.copyOfRange(tail, slotsAvailable - cells.length, slotsAvailable);
        } else {
            // All of tail is displaced, plus the excess inserted cells
            int excess = cells.length - slotsAvailable;
            spill = new Cell[excess + tail.length];
            System.arraycopy(cells, slotsAvailable, spill, 0, excess);
            System.arraycopy(tail, 0, spill, excess, tail.length);
        }

        if (hasContent(spill)) {
            int nextRow = row + 1;
            if (nextRow >= height) {
                scrollUp();
                nextRow = height - 1;
            }
            insertCellsAt(nextRow, 0, spill);
        }
    }

    private static boolean hasContent(Cell[] cells) {
        for (Cell c : cells) {
            if (!c.isEmpty()) return true;
        }
        return false;
    }

    private void scrollUp() {
        if (maxScrollback > 0) {
            if (scrollback.size() >= maxScrollback) {
                scrollback.pollFirst();
            }
            scrollback.addLast(screen[0]);
        }
        System.arraycopy(screen, 1, screen, 0, height - 1);
        screen[height - 1] = new TerminalLine(width);
    }

    public String getScreenContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(screen[i].toString());
        }
        return sb.toString();
    }

    public String getFullContent() {
        StringBuilder sb = new StringBuilder();
        TerminalLine[] sbLines = getScrollbackLines();
        for (TerminalLine line : sbLines) {
            sb.append(line.toString()).append('\n');
        }
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(screen[i].toString());
        }
        return sb.toString();
    }

    public int getTotalRows() {
        return scrollback.size() + height;
    }

    public char getCharAt(int row, int col) {
        return resolveRow(row, col).getCell(col).character();
    }

    public CellAttributes getAttributesAt(int row, int col) {
        return resolveRow(row, col).getCell(col).attributes();
    }

    public String getLine(int row) {
        TerminalLine line = resolveRow(row, 0);
        StringBuilder sb = new StringBuilder(width);
        for (int col = 0; col < width; col++) {
            sb.append(line.getCell(col).character());
        }
        return sb.toString();
    }

    /** Resolves a unified row index into a TerminalLine. Also validates {@code col} if >= 0. */
    private TerminalLine resolveRow(int row, int col) {
        int total = scrollback.size() + height;
        if (row < 0 || row >= total) {
            throw new IndexOutOfBoundsException(
                    "row " + row + " out of bounds for total rows " + total);
        }
        if (col < 0 || col >= width) {
            throw new IndexOutOfBoundsException(
                    "col " + col + " out of bounds for width " + width);
        }
        int sb = scrollback.size();
        if (row < sb) {
            return getScrollbackLines()[row];
        }
        return screen[row - sb];
    }

    private static void requireNonNegative(int n, String name) {
        if (n < 0) throw new IllegalArgumentException(name + " must be >= 0, got: " + n);
    }
}
