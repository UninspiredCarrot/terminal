package com.terminal.buffer;

import com.terminal.model.Cell;
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

    // --- write ---

    @Test
    void writeUpdatesCorrectCellsAndAdvancesCursor() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setCursor(2, 3);
        buffer.write("Hi");

        assertThat(buffer.getScreen()[2].getCell(3).character()).isEqualTo('H');
        assertThat(buffer.getScreen()[2].getCell(4).character()).isEqualTo('i');
        assertThat(buffer.getCursorRow()).isEqualTo(2);
        assertThat(buffer.getCursorCol()).isEqualTo(5);
    }

    @Test
    void writeAppliesCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(Color.RED).withStyle(Style.BOLD);
        buffer.setCurrentAttributes(attrs);
        buffer.write("X");

        assertThat(buffer.getScreen()[0].getCell(0).attributes()).isEqualTo(attrs);
    }

    @Test
    void writeWrapsToNextLineAtRightEdge() {
        // width=5; write 6 chars starting at col 0 → last char lands on row 1 col 0
        TerminalBuffer buffer = new TerminalBuffer(5, 4, 100);
        buffer.write("ABCDEF");

        assertThat(buffer.getScreen()[0].getCell(4).character()).isEqualTo('E');
        assertThat(buffer.getScreen()[1].getCell(0).character()).isEqualTo('F');
        assertThat(buffer.getCursorRow()).isEqualTo(1);
        assertThat(buffer.getCursorCol()).isEqualTo(1);
    }

    @Test
    void writeTriggersScrollWhenWrappingPastLastRow() {
        // height=2, width=5; fill both rows then write one more char
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        mark(buffer.getScreen()[0], 'T'); // top line we expect to scroll off
        buffer.setCursor(1, 4);           // last col of last row
        buffer.write("X");               // fills (1,4), then wraps → scroll, cursor=(1,0)

        assertThat(buffer.getScrollbackSize()).isEqualTo(1);
        assertThat(buffer.getCursorRow()).isEqualTo(1);
        assertThat(buffer.getCursorCol()).isEqualTo(0);
    }

    @Test
    void writeFromLastColOfLastRowScrollsAndWraps() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        mark(buffer.getScreen()[0], 'F'); // this should scroll to scrollback
        buffer.setCursor(2, 4);
        buffer.write("Z");

        // Z written at (2,4); wrap → scroll → the row shifts to [1], new empty line at [2]
        assertThat(buffer.getScreen()[1].getCell(4).character()).isEqualTo('Z');
        assertThat(buffer.getScrollbackSize()).isEqualTo(1);
        assertThat(markerOf(buffer.getScrollbackLines()[0])).isEqualTo('F');
        assertThat(buffer.getCursorRow()).isEqualTo(2);
        assertThat(buffer.getCursorCol()).isEqualTo(0);
    }

    @Test
    void writeNullIsNoOp() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.write(null);
        assertThat(buffer.getCursorRow()).isZero();
        assertThat(buffer.getCursorCol()).isZero();
        assertThat(buffer.getScreen()[0].getCell(0).isEmpty()).isTrue();
    }

    @Test
    void writeEmptyStringIsNoOp() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setCursor(2, 3);
        buffer.write("");
        assertThat(buffer.getCursorRow()).isEqualTo(2);
        assertThat(buffer.getCursorCol()).isEqualTo(3);
    }

    // --- insert ---

    /** Fills a line with single-char cells using the given character and default attributes. */
    private static void fillLine(TerminalLine line, String content) {
        for (int i = 0; i < content.length(); i++) {
            line.setCell(i, new com.terminal.model.Cell(content.charAt(i), CellAttributes.DEFAULT));
        }
    }

    @Test
    void insertMidLineShiftsExistingContentRight() {
        // width=8: line1 = [A,B,C,D,E,_,_,_], insert "XY" at col 2
        TerminalBuffer buffer = new TerminalBuffer(8, 3, 100);
        fillLine(buffer.getScreen()[1], "ABCDE");
        buffer.setCursor(1, 2);

        buffer.insert("XY");

        TerminalLine line = buffer.getScreen()[1];
        assertThat(line.getCell(2).character()).isEqualTo('X');
        assertThat(line.getCell(3).character()).isEqualTo('Y');
        assertThat(line.getCell(4).character()).isEqualTo('C');
        assertThat(line.getCell(5).character()).isEqualTo('D');
        assertThat(line.getCell(6).character()).isEqualTo('E');
    }

    @Test
    void insertedCellsHaveCurrentAttributesShiftedCellsRetainOriginalAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(8, 3, 100);
        CellAttributes original = CellAttributes.DEFAULT.withForeground(Color.RED);
        CellAttributes inserted = CellAttributes.DEFAULT.withForeground(Color.BLUE);

        // Fill cols 0-2 with RED attributes
        for (int c = 0; c < 3; c++) {
            buffer.getScreen()[0].setCell(c,
                    new com.terminal.model.Cell('A', original));
        }

        buffer.setCurrentAttributes(inserted);
        buffer.setCursor(0, 0);
        buffer.insert("X");

        assertThat(buffer.getScreen()[0].getCell(0).attributes()).isEqualTo(inserted);
        assertThat(buffer.getScreen()[0].getCell(1).attributes()).isEqualTo(original);
        assertThat(buffer.getScreen()[0].getCell(2).attributes()).isEqualTo(original);
    }

    @Test
    void insertUpdatesCursorToOnePassLastInsertedChar() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.setCursor(1, 3);
        buffer.insert("ABC");
        assertThat(buffer.getCursorRow()).isEqualTo(1);
        assertThat(buffer.getCursorCol()).isEqualTo(6);
    }

    @Test
    void insertAtColZeroOfEmptyLineNoSpill() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.insert("Hi");
        assertThat(buffer.getScreen()[0].getCell(0).character()).isEqualTo('H');
        assertThat(buffer.getScreen()[0].getCell(1).character()).isEqualTo('i');
        assertThat(buffer.getScrollbackSize()).isZero();
        assertThat(buffer.getCursorRow()).isEqualTo(0);
        assertThat(buffer.getCursorCol()).isEqualTo(2);
    }

    @Test
    void insertOverflowSpillsToNextLine() {
        // width=5: line0=[A,B,C,D,E], insert "XY" at col 2
        // line0 → [A,B,X,Y,C], spill [D,E] → line1
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        fillLine(buffer.getScreen()[0], "ABCDE");
        buffer.setCursor(0, 2);

        buffer.insert("XY");

        assertThat(buffer.getScreen()[0].getCell(2).character()).isEqualTo('X');
        assertThat(buffer.getScreen()[0].getCell(3).character()).isEqualTo('Y');
        assertThat(buffer.getScreen()[0].getCell(4).character()).isEqualTo('C');
        assertThat(buffer.getScreen()[1].getCell(0).character()).isEqualTo('D');
        assertThat(buffer.getScreen()[1].getCell(1).character()).isEqualTo('E');
    }

    @Test
    void insertSpillIsRecursivePushesNextLineRight() {
        // width=5, height=3: line0=[ABCDE], line1=[FGHIJ]
        // insert "X" at (0,0): line0→[X,A,B,C,D] spill [E] → line1
        // line1→[E,F,G,H,I] spill [J] → line2
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        fillLine(buffer.getScreen()[0], "ABCDE");
        fillLine(buffer.getScreen()[1], "FGHIJ");
        buffer.setCursor(0, 0);

        buffer.insert("X");

        assertThat(buffer.getScreen()[0].getCell(0).character()).isEqualTo('X');
        assertThat(buffer.getScreen()[0].getCell(4).character()).isEqualTo('D');
        assertThat(buffer.getScreen()[1].getCell(0).character()).isEqualTo('E');
        assertThat(buffer.getScreen()[1].getCell(4).character()).isEqualTo('I');
        assertThat(buffer.getScreen()[2].getCell(0).character()).isEqualTo('J');
    }

    @Test
    void insertSpillPastLastRowTriggersScroll() {
        // width=5, height=2: line0=[ABCDE], line1=[FGHIJ]
        // insert "X" at (0,0): line0→[X,A,B,C,D], spill [E] → line1
        // line1→[E,F,G,H,I], spill [J] → row 2 >= height → scrollUp
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        fillLine(buffer.getScreen()[0], "ABCDE");
        fillLine(buffer.getScreen()[1], "FGHIJ");
        buffer.setCursor(0, 0);

        buffer.insert("X");

        assertThat(buffer.getScrollbackSize()).isEqualTo(1);
        // After scroll, line0 (with X,A,B,C,D) goes to scrollback;
        // screen[0] = [E,F,G,H,I], screen[1] = [J,_,_,_,_]
        assertThat(buffer.getScreen()[0].getCell(0).character()).isEqualTo('E');
        assertThat(buffer.getScreen()[1].getCell(0).character()).isEqualTo('J');
    }

    @Test
    void insertNullIsNoOp() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.setCursor(1, 2);
        buffer.insert(null);
        assertThat(buffer.getCursorRow()).isEqualTo(1);
        assertThat(buffer.getCursorCol()).isEqualTo(2);
    }

    @Test
    void insertEmptyStringIsNoOp() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 100);
        buffer.setCursor(1, 2);
        buffer.insert("");
        assertThat(buffer.getCursorRow()).isEqualTo(1);
        assertThat(buffer.getCursorCol()).isEqualTo(2);
    }

    // --- fillLine ---

    @Test
    void fillLineSetsEveryCell() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(Color.CYAN);

        buffer.fillLine(1, 'X', attrs);

        TerminalLine line = buffer.getScreen()[1];
        for (int col = 0; col < buffer.getWidth(); col++) {
            assertThat(line.getCell(col).character()).as("col %d char", col).isEqualTo('X');
            assertThat(line.getCell(col).attributes()).as("col %d attrs", col).isEqualTo(attrs);
        }
    }

    @Test
    void fillLineDoesNotMoveCursor() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        buffer.setCursor(2, 3);
        buffer.fillLine(0, 'Z', CellAttributes.DEFAULT);
        assertThat(buffer.getCursorRow()).isEqualTo(2);
        assertThat(buffer.getCursorCol()).isEqualTo(3);
    }

    @Test
    void fillLineWithSpaceClearsLineWithGivenAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        fillLine(buffer.getScreen()[0], "ABCDE");
        CellAttributes clearAttrs = CellAttributes.DEFAULT.withBackground(Color.BLUE);

        buffer.fillLine(0, ' ', clearAttrs);

        TerminalLine line = buffer.getScreen()[0];
        for (int col = 0; col < buffer.getWidth(); col++) {
            assertThat(line.getCell(col).isEmpty()).as("col %d isEmpty", col).isTrue();
            assertThat(line.getCell(col).attributes()).as("col %d attrs", col).isEqualTo(clearAttrs);
        }
    }

    @ParameterizedTest(name = "row={0}")
    @ValueSource(ints = {-1, -10, 3, 4})
    void fillLineRowOutOfBoundsThrowsIllegalArgumentException(int row) {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buffer.fillLine(row, 'X', CellAttributes.DEFAULT));
    }

    @Test
    void fillLineNullAttributesThrowsNullPointerException() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        assertThatNullPointerException()
                .isThrownBy(() -> buffer.fillLine(0, 'X', null));
    }

    // --- clearScreen / clearAll ---

    @Test
    void clearScreenResetsAllCellsAndCursor() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        buffer.setCursor(2, 4);
        buffer.write("HELLO");
        buffer.insertLineAtBottom(); // put something in scrollback

        buffer.clearScreen();

        assertThat(buffer.getCursorRow()).isZero();
        assertThat(buffer.getCursorCol()).isZero();
        for (int row = 0; row < buffer.getHeight(); row++) {
            for (int col = 0; col < buffer.getWidth(); col++) {
                Cell cell = buffer.getScreen()[row].getCell(col);
                assertThat(cell.character()).as("row %d col %d char", row, col).isEqualTo(' ');
                assertThat(cell.attributes()).as("row %d col %d attrs", row, col)
                        .isEqualTo(CellAttributes.DEFAULT);
            }
        }
    }

    @Test
    void clearScreenPreservesScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.insertLineAtBottom();
        buffer.insertLineAtBottom();
        int scrollbackBefore = buffer.getScrollbackSize();

        buffer.clearScreen();

        assertThat(buffer.getScrollbackSize()).isEqualTo(scrollbackBefore);
    }

    @Test
    void clearAllClearsBothScreenAndScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.write("HI");
        buffer.insertLineAtBottom();
        buffer.insertLineAtBottom();

        buffer.clearAll();

        assertThat(buffer.getScrollbackSize()).isZero();
        assertThat(buffer.getCursorRow()).isZero();
        assertThat(buffer.getCursorCol()).isZero();
        for (int row = 0; row < buffer.getHeight(); row++) {
            for (int col = 0; col < buffer.getWidth(); col++) {
                assertThat(buffer.getScreen()[row].getCell(col).isEmpty())
                        .as("row %d col %d", row, col).isTrue();
            }
        }
    }

    // --- unified coordinate system ---

    @Test
    void freshBufferRow0MapsToScreenRow0() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        buffer.getScreen()[0].setCell(0, new Cell('Q', CellAttributes.DEFAULT));

        assertThat(buffer.getTotalRows()).isEqualTo(3);
        assertThat(buffer.getCharAt(0, 0)).isEqualTo('Q');
    }

    @Test
    void afterScrollingRow0IsOldestScrollbackLine() {
        // width=5, height=2; mark each line before it scrolls off
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.getScreen()[0].setCell(0, new Cell('A', CellAttributes.DEFAULT));
        buffer.insertLineAtBottom(); // 'A' line → scrollback[0]
        buffer.getScreen()[0].setCell(0, new Cell('B', CellAttributes.DEFAULT));
        buffer.insertLineAtBottom(); // 'B' line → scrollback[1]

        // scrollbackSize=2, height=2 → totalRows=4
        // unified row 0 = scrollback[0] ('A'), row 2 = screen[0]
        assertThat(buffer.getTotalRows()).isEqualTo(4);
        assertThat(buffer.getCharAt(0, 0)).isEqualTo('A');
        assertThat(buffer.getCharAt(1, 0)).isEqualTo('B');
        assertThat(buffer.getScrollbackSize()).isEqualTo(2);
    }

    @Test
    void screenRowsMappedCorrectlyWithScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.insertLineAtBottom(); // scrollbackSize becomes 1
        buffer.getScreen()[0].setCell(0, new Cell('S', CellAttributes.DEFAULT));

        // unified row 0 = scrollback[0], unified row 1 = screen[0]
        int sb = buffer.getScrollbackSize();
        assertThat(buffer.getCharAt(sb, 0)).isEqualTo('S');
    }

    @Test
    void getCharAtReadsFromBothScrollbackAndScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(Color.GREEN);
        buffer.getScreen()[0].setCell(2, new Cell('X', attrs));
        buffer.insertLineAtBottom(); // 'X' line scrolls off → scrollback[0]
        buffer.getScreen()[0].setCell(1, new Cell('Y', CellAttributes.DEFAULT));

        assertThat(buffer.getCharAt(0, 2)).isEqualTo('X'); // scrollback
        assertThat(buffer.getCharAt(1, 1)).isEqualTo('Y'); // screen
    }

    @Test
    void getAttributesAtReadsFromBothScrollbackAndScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        CellAttributes sbAttrs = CellAttributes.DEFAULT.withForeground(Color.RED);
        CellAttributes scAttrs = CellAttributes.DEFAULT.withForeground(Color.BLUE);
        buffer.getScreen()[0].setCell(0, new Cell('A', sbAttrs));
        buffer.insertLineAtBottom();
        buffer.getScreen()[0].setCell(0, new Cell('B', scAttrs));

        assertThat(buffer.getAttributesAt(0, 0)).isEqualTo(sbAttrs); // scrollback
        assertThat(buffer.getAttributesAt(1, 0)).isEqualTo(scAttrs); // screen
    }

    @Test
    void getLineReturnsFullWidthStringFromScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        fillLine(buffer.getScreen()[0], "ABCDE");
        buffer.insertLineAtBottom();

        assertThat(buffer.getLine(0)).isEqualTo("ABCDE");
    }

    @Test
    void getLineReturnsFullWidthStringFromScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 100);
        buffer.insertLineAtBottom(); // scrollbackSize=1
        fillLine(buffer.getScreen()[0], "HELLO");

        assertThat(buffer.getLine(1)).isEqualTo("HELLO");
    }

    @ParameterizedTest(name = "row={0}")
    @ValueSource(ints = {-1, 3})
    void getCharAtOutOfBoundsRowThrowsIndexOutOfBoundsException(int row) {
        // height=3, no scrollback → totalRows=3; valid rows: 0,1,2
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> buffer.getCharAt(row, 0));
    }

    @ParameterizedTest(name = "col={0}")
    @ValueSource(ints = {-1, 5})
    void getCharAtOutOfBoundsColThrowsIndexOutOfBoundsException(int col) {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> buffer.getCharAt(0, col));
    }

    @ParameterizedTest(name = "row={0}")
    @ValueSource(ints = {-1, 3})
    void getLineOutOfBoundsRowThrowsIndexOutOfBoundsException(int row) {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 100);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> buffer.getLine(row));
    }
}
