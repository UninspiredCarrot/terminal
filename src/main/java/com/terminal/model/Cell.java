package com.terminal.model;

import java.util.Objects;

public final class Cell {

    private char character;
    private CellAttributes attributes;

    public Cell(char character, CellAttributes attributes) {
        this.character = character;
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    public Cell() {
        this(' ', CellAttributes.DEFAULT);
    }

    public char character() { return character; }
    public CellAttributes attributes() { return attributes; }

    public void setCharacter(char character) {
        this.character = character;
    }

    public void setAttributes(CellAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes must not be null");
    }

    public boolean isEmpty() {
        return character == ' ' || character == '\0';
    }

    public void reset() {
        this.character = ' ';
        this.attributes = CellAttributes.DEFAULT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell other)) return false;
        return character == other.character && attributes.equals(other.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, attributes);
    }

    @Override
    public String toString() {
        return "Cell{character='" + character + "', attributes=" + attributes + "}";
    }
}
