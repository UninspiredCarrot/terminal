package com.terminal.buffer;

import com.terminal.model.CellAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
}
