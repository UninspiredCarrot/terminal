package com.terminal.buffer;

import com.terminal.model.CellAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.terminal.model.Color;
import com.terminal.model.Style;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TerminalBufferTest {

    @Test
    void constructorCreatesCorrectScreenDimensions() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThat(buffer.getWidth()).isEqualTo(80);
        assertThat(buffer.getHeight()).isEqualTo(24);
        assertThat(buffer.getMaxScrollback()).isEqualTo(1000);
    }

    @Test
    void allScreenLinesAreInitializedWithDefaultEmptyCells() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        for (int row = 0; row < buffer.getHeight(); row++) {
            TerminalLine line = buffer.getScreen()[row];
            for (int col = 0; col < buffer.getWidth(); col++) {
                assertThat(line.getCell(col).character())
                        .as("row %d col %d character", row, col)
                        .isEqualTo(' ');
                assertThat(line.getCell(col).attributes())
                        .as("row %d col %d attributes", row, col)
                        .isEqualTo(CellAttributes.DEFAULT);
            }
        }
    }

    @Test
    void cursorStartsAtOrigin() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThat(buffer.getCursorRow()).isZero();
        assertThat(buffer.getCursorCol()).isZero();
    }

    @Test
    void scrollbackIsInitiallyEmpty() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThat(buffer.getScrollbackSize()).isZero();
    }

    @ParameterizedTest(name = "row={0}, col={1}")
    @CsvSource({
        "0,  0",
        "23, 79",
        "12, 40",
    })
    void setCursorUpdatesPosition(int row, int col) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(row, col);
        assertThat(buffer.getCursorRow()).isEqualTo(row);
        assertThat(buffer.getCursorCol()).isEqualTo(col);
    }

    @ParameterizedTest(name = "row={0}")
    @ValueSource(ints = {-1, -100, 24, 25})
    void setCursorThrowsForInvalidRow(int row) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.setCursor(row, 0));
    }

    @ParameterizedTest(name = "col={0}")
    @ValueSource(ints = {-1, -100, 80, 81})
    void setCursorThrowsForInvalidCol(int col) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.setCursor(0, col));
    }

    // --- moveCursor* ---

    @Test
    void moveCursorUpDecreasesRow() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(10, 40);
        buffer.moveCursorUp(3);
        assertThat(buffer.getCursorRow()).isEqualTo(7);
        assertThat(buffer.getCursorCol()).isEqualTo(40);
    }

    @Test
    void moveCursorDownIncreasesRow() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(10, 40);
        buffer.moveCursorDown(5);
        assertThat(buffer.getCursorRow()).isEqualTo(15);
        assertThat(buffer.getCursorCol()).isEqualTo(40);
    }

    @Test
    void moveCursorLeftDecreasesCol() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(10, 40);
        buffer.moveCursorLeft(10);
        assertThat(buffer.getCursorCol()).isEqualTo(30);
        assertThat(buffer.getCursorRow()).isEqualTo(10);
    }

    @Test
    void moveCursorRightIncreasesCol() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(10, 40);
        buffer.moveCursorRight(15);
        assertThat(buffer.getCursorCol()).isEqualTo(55);
        assertThat(buffer.getCursorRow()).isEqualTo(10);
    }

    @Test
    void moveCursorUpClampsAtRowZero() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(3, 5);
        buffer.moveCursorUp(100);
        assertThat(buffer.getCursorRow()).isZero();
    }

    @Test
    void moveCursorDownClampsAtLastRow() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(20, 5);
        buffer.moveCursorDown(100);
        assertThat(buffer.getCursorRow()).isEqualTo(23);
    }

    @Test
    void moveCursorLeftClampsAtColumnZero() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(5, 3);
        buffer.moveCursorLeft(100);
        assertThat(buffer.getCursorCol()).isZero();
    }

    @Test
    void moveCursorRightClampsAtLastColumn() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(5, 70);
        buffer.moveCursorRight(100);
        assertThat(buffer.getCursorCol()).isEqualTo(79);
    }

    @Test
    void moveCursorByZeroIsNoOp() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        buffer.setCursor(10, 20);
        buffer.moveCursorUp(0);
        buffer.moveCursorDown(0);
        buffer.moveCursorLeft(0);
        buffer.moveCursorRight(0);
        assertThat(buffer.getCursorRow()).isEqualTo(10);
        assertThat(buffer.getCursorCol()).isEqualTo(20);
    }

    @ParameterizedTest(name = "n={0}")
    @ValueSource(ints = {-1, -10})
    void moveCursorUpNegativeThrows(int n) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.moveCursorUp(n));
    }

    @ParameterizedTest(name = "n={0}")
    @ValueSource(ints = {-1, -10})
    void moveCursorDownNegativeThrows(int n) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.moveCursorDown(n));
    }

    @ParameterizedTest(name = "n={0}")
    @ValueSource(ints = {-1, -10})
    void moveCursorLeftNegativeThrows(int n) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.moveCursorLeft(n));
    }

    @ParameterizedTest(name = "n={0}")
    @ValueSource(ints = {-1, -10})
    void moveCursorRightNegativeThrows(int n) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.moveCursorRight(n));
    }

    @ParameterizedTest(name = "width={0}, height={1}, maxScrollback={2}")
    @CsvSource({
        "0,  24, 1000",
        "-1, 24, 1000",
        "80,  0, 1000",
        "80, -1, 1000",
        "80, 24,    0",
        "80, 24,   -1",
    })
    void invalidConstructorArgsThrowIllegalArgumentException(int width, int height, int maxScrollback) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new TerminalBuffer(width, height, maxScrollback));
    }

    // --- currentAttributes ---

    @Test
    void defaultCurrentAttributesIsCellAttributesDefault() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThat(buffer.getCurrentAttributes()).isEqualTo(CellAttributes.DEFAULT);
    }

    @Test
    void setAndGetCurrentAttributesRoundTrips() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        CellAttributes attrs = CellAttributes.DEFAULT
                .withForeground(Color.GREEN)
                .withStyle(Style.BOLD);
        buffer.setCurrentAttributes(attrs);
        assertThat(buffer.getCurrentAttributes()).isEqualTo(attrs);
    }

    @Test
    void setCurrentAttributesNullThrowsNullPointerException() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertThatNullPointerException()
                .isThrownBy(() -> buffer.setCurrentAttributes(null));
    }

    // --- insertLineAtBottom / scrollUp ---

    /** Stamps column 0 of the given line with a marker character for identity tracking. */
    private static void mark(TerminalLine line, char marker) {
        line.setCell(0, new com.terminal.model.Cell(marker, CellAttributes.DEFAULT));
    }

    private static char markerOf(TerminalLine line) {
        return line.getCell(0).character();
    }

    @Test
    void insertLineAtBottomMovesTopLineToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);
        mark(buffer.getScreen()[0], 'A');

        buffer.insertLineAtBottom();

        assertThat(buffer.getScrollbackSize()).isEqualTo(1);
        assertThat(markerOf(buffer.getScrollbackLines()[0])).isEqualTo('A');
    }

    @Test
    void insertLineAtBottomPlacesFreshEmptyLineAtBottom() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);
        mark(buffer.getScreen()[2], 'Z');

        buffer.insertLineAtBottom();

        TerminalLine newBottom = buffer.getScreen()[2];
        for (int col = 0; col < buffer.getWidth(); col++) {
            assertThat(newBottom.getCell(col).isEmpty())
                    .as("col %d should be empty", col)
                    .isTrue();
        }
    }

    @Test
    void insertLineAtBottomShiftsContentUp() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);
        mark(buffer.getScreen()[0], 'A');
        mark(buffer.getScreen()[1], 'B');
        mark(buffer.getScreen()[2], 'C');

        buffer.insertLineAtBottom();

        // A scrolled off; B is now row 0, C is now row 1
        assertThat(markerOf(buffer.getScreen()[0])).isEqualTo('B');
        assertThat(markerOf(buffer.getScreen()[1])).isEqualTo('C');
    }

    @Test
    void insertLineAtBottomScrollbackGrowsByOne() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);
        buffer.insertLineAtBottom();
        assertThat(buffer.getScrollbackSize()).isEqualTo(1);
        buffer.insertLineAtBottom();
        assertThat(buffer.getScrollbackSize()).isEqualTo(2);
    }

    @Test
    void scrollbackDropsOldestWhenMaxExceeded() {
        int maxScrollback = 3;
        TerminalBuffer buffer = new TerminalBuffer(5, 2, maxScrollback);

        // Mark each row before it scrolls off so we can identify it
        for (char c = 'A'; c <= 'E'; c++) {
            mark(buffer.getScreen()[0], c);
            buffer.insertLineAtBottom();
        }

        // 5 insertions with max=3: scrollback holds the 3 most recent evicted lines (C, D, E)
        assertThat(buffer.getScrollbackSize()).isEqualTo(maxScrollback);
        TerminalLine[] lines = buffer.getScrollbackLines();
        assertThat(markerOf(lines[0])).isEqualTo('C');
        assertThat(markerOf(lines[1])).isEqualTo('D');
        assertThat(markerOf(lines[2])).isEqualTo('E');
    }

    @Test
    void screenHeightRemainsConstantAfterScrolling() {
        TerminalBuffer buffer = new TerminalBuffer(5, 4, 10);
        for (int i = 0; i < 20; i++) {
            buffer.insertLineAtBottom();
        }
        assertThat(buffer.getHeight()).isEqualTo(4);
        assertThat(buffer.getScreen()).hasSize(4);
    }
}
