package com.terminal.buffer;

import com.terminal.model.Cell;
import com.terminal.model.CellAttributes;
import com.terminal.model.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TerminalLineTest {

    @Test
    void constructionFillsAllCellsWithDefaults() {
        TerminalLine line = new TerminalLine(5);
        for (int i = 0; i < 5; i++) {
            Cell cell = line.getCell(i);
            assertThat(cell.character()).as("column %d character", i).isEqualTo(' ');
            assertThat(cell.attributes()).as("column %d attributes", i).isEqualTo(CellAttributes.DEFAULT);
        }
    }

    @Test
    void getWidthReturnsConstructorValue() {
        assertThat(new TerminalLine(80).getWidth()).isEqualTo(80);
    }

    @Test
    void getCellAndSetCellWorkForValidIndices() {
        TerminalLine line = new TerminalLine(10);
        Cell cell = new Cell('X', CellAttributes.DEFAULT.withForeground(Color.RED));

        line.setCell(0, cell);
        assertThat(line.getCell(0)).isSameAs(cell);

        line.setCell(9, cell);
        assertThat(line.getCell(9)).isSameAs(cell);
    }

    @Test
    void getCellThrowsForNegativeColumn() {
        TerminalLine line = new TerminalLine(10);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> line.getCell(-1));
    }

    @Test
    void getCellThrowsForColumnEqualToWidth() {
        TerminalLine line = new TerminalLine(10);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> line.getCell(10));
    }

    @Test
    void setCellThrowsForNegativeColumn() {
        TerminalLine line = new TerminalLine(10);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> line.setCell(-1, new Cell()));
    }

    @Test
    void setCellThrowsForColumnEqualToWidth() {
        TerminalLine line = new TerminalLine(10);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> line.setCell(10, new Cell()));
    }

    @Test
    void setCellThrowsForNullCell() {
        TerminalLine line = new TerminalLine(10);
        assertThatNullPointerException()
                .isThrownBy(() -> line.setCell(0, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void constructorThrowsForInvalidWidth(int width) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new TerminalLine(width))
                .withMessageContaining(String.valueOf(width));
    }
}
