package com.terminal.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CellTest {

    @Test
    void defaultConstructorProducesEmptyCellWithDefaultAttributes() {
        Cell cell = new Cell();
        assertThat(cell.character()).isEqualTo(' ');
        assertThat(cell.attributes()).isEqualTo(CellAttributes.DEFAULT);
        assertThat(cell.isEmpty()).isTrue();
    }

    @Test
    void settersUpdateFieldsInPlace() {
        Cell cell = new Cell();
        CellAttributes attrs = CellAttributes.DEFAULT.withForeground(Color.RED);

        cell.setCharacter('X');
        cell.setAttributes(attrs);

        assertThat(cell.character()).isEqualTo('X');
        assertThat(cell.attributes()).isEqualTo(attrs);
    }

    @Test
    void isEmptyTrueForSpace() {
        assertThat(new Cell(' ', CellAttributes.DEFAULT).isEmpty()).isTrue();
    }

    @Test
    void isEmptyTrueForNullChar() {
        assertThat(new Cell('\0', CellAttributes.DEFAULT).isEmpty()).isTrue();
    }

    @Test
    void isEmptyFalseForNonEmptyChar() {
        assertThat(new Cell('A', CellAttributes.DEFAULT).isEmpty()).isFalse();
    }

    @Test
    void resetRestoresDefaults() {
        Cell cell = new Cell('Z', CellAttributes.DEFAULT.withForeground(Color.GREEN));
        cell.reset();

        assertThat(cell.character()).isEqualTo(' ');
        assertThat(cell.attributes()).isEqualTo(CellAttributes.DEFAULT);
        assertThat(cell.isEmpty()).isTrue();
    }

    @Test
    void nullAttributesInConstructorThrowsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Cell('A', null))
                .withMessageContaining("attributes");
    }

    @Test
    void nullAttributesInSetterThrowsNullPointerException() {
        Cell cell = new Cell();
        assertThatNullPointerException()
                .isThrownBy(() -> cell.setAttributes(null))
                .withMessageContaining("attributes");
    }

    @Test
    void equalCellsHaveSameHashCode() {
        Cell a = new Cell('X', CellAttributes.DEFAULT.withStyle(Style.BOLD));
        Cell b = new Cell('X', CellAttributes.DEFAULT.withStyle(Style.BOLD));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringContainsCharacterAndAttributes() {
        Cell cell = new Cell('Q', CellAttributes.DEFAULT);
        assertThat(cell.toString()).contains("Q");
    }
}
