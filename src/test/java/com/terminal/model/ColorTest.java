package com.terminal.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorTest {

    @Test
    void shouldHaveExactlySeventeenValues() {
        assertThat(Color.values()).hasSize(17);
    }

    @Test
    void shouldContainDefault() {
        assertThat(Color.values()).contains(Color.DEFAULT);
    }
}
