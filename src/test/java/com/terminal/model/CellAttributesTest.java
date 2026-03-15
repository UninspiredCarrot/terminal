package com.terminal.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CellAttributesTest {

    @Test
    void defaultHasDefaultColorsAndNoStyles() {
        CellAttributes attr = CellAttributes.DEFAULT;
        assertThat(attr.foreground()).isEqualTo(Color.DEFAULT);
        assertThat(attr.background()).isEqualTo(Color.DEFAULT);
        assertThat(attr.styles()).isZero();
    }

    @Test
    void hasStyleReturnsFalseWhenNotSet() {
        assertThat(CellAttributes.DEFAULT.hasStyle(Style.BOLD)).isFalse();
        assertThat(CellAttributes.DEFAULT.hasStyle(Style.ITALIC)).isFalse();
        assertThat(CellAttributes.DEFAULT.hasStyle(Style.UNDERLINE)).isFalse();
    }

    @Test
    void hasStyleReturnsTrueForEachFlag() {
        for (Style s : Style.values()) {
            CellAttributes attr = CellAttributes.DEFAULT.withStyle(s);
            assertThat(attr.hasStyle(s))
                    .as("hasStyle(%s)", s)
                    .isTrue();
        }
    }

    @Test
    void withForegroundReturnsNewInstanceWithUpdatedForeground() {
        CellAttributes original = CellAttributes.DEFAULT;
        CellAttributes updated = original.withForeground(Color.RED);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.foreground()).isEqualTo(Color.RED);
        assertThat(updated.background()).isEqualTo(original.background());
        assertThat(updated.styles()).isEqualTo(original.styles());
    }

    @Test
    void withBackgroundReturnsNewInstanceWithUpdatedBackground() {
        CellAttributes original = CellAttributes.DEFAULT;
        CellAttributes updated = original.withBackground(Color.BLUE);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.background()).isEqualTo(Color.BLUE);
        assertThat(updated.foreground()).isEqualTo(original.foreground());
        assertThat(updated.styles()).isEqualTo(original.styles());
    }

    @Test
    void withStylesReturnsNewInstanceWithUpdatedStylesBitmask() {
        int mask = Style.BOLD.bit() | Style.UNDERLINE.bit();
        CellAttributes updated = CellAttributes.DEFAULT.withStyles(mask);

        assertThat(updated).isNotSameAs(CellAttributes.DEFAULT);
        assertThat(updated.styles()).isEqualTo(mask);
        assertThat(updated.hasStyle(Style.BOLD)).isTrue();
        assertThat(updated.hasStyle(Style.UNDERLINE)).isTrue();
        assertThat(updated.hasStyle(Style.ITALIC)).isFalse();
    }

    @Test
    void withStyleAddsFlag() {
        CellAttributes withBold = CellAttributes.DEFAULT.withStyle(Style.BOLD);
        assertThat(withBold.hasStyle(Style.BOLD)).isTrue();
        assertThat(withBold.hasStyle(Style.ITALIC)).isFalse();

        CellAttributes withBothBoldAndItalic = withBold.withStyle(Style.ITALIC);
        assertThat(withBothBoldAndItalic.hasStyle(Style.BOLD)).isTrue();
        assertThat(withBothBoldAndItalic.hasStyle(Style.ITALIC)).isTrue();
    }

    @Test
    void withoutStyleRemovesFlag() {
        CellAttributes withBoldAndItalic = CellAttributes.DEFAULT
                .withStyle(Style.BOLD)
                .withStyle(Style.ITALIC);

        CellAttributes withoutBold = withBoldAndItalic.withoutStyle(Style.BOLD);
        assertThat(withoutBold.hasStyle(Style.BOLD)).isFalse();
        assertThat(withoutBold.hasStyle(Style.ITALIC)).isTrue();
    }

    @Test
    void withoutStyleOnUnsetFlagIsIdempotent() {
        CellAttributes attr = CellAttributes.DEFAULT.withoutStyle(Style.BOLD);
        assertThat(attr.hasStyle(Style.BOLD)).isFalse();
        assertThat(attr.styles()).isZero();
    }

    @Test
    void nullForegroundThrowsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CellAttributes(null, Color.DEFAULT, 0))
                .withMessageContaining("foreground");
    }

    @Test
    void nullBackgroundThrowsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> new CellAttributes(Color.DEFAULT, null, 0))
                .withMessageContaining("background");
    }

    @Test
    void equalInstancesHaveSameHashCode() {
        CellAttributes a = new CellAttributes(Color.RED, Color.BLUE, Style.BOLD.bit());
        CellAttributes b = new CellAttributes(Color.RED, Color.BLUE, Style.BOLD.bit());
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringContainsAllFields() {
        CellAttributes attr = new CellAttributes(Color.GREEN, Color.BLACK, Style.ITALIC.bit());
        String s = attr.toString();
        assertThat(s).contains("GREEN", "BLACK", String.valueOf(Style.ITALIC.bit()));
    }
}
