package com.terminal;

import com.terminal.buffer.TerminalBuffer;
import com.terminal.model.CellAttributes;
import com.terminal.model.Color;
import com.terminal.model.Style;

public class Main {

    public static void main(String[] args) {

        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);

        // ── Step 1: bold red text ─────────────────────────────────────────────
        System.out.println("--- Writing bold red text at row 0 ---");
        CellAttributes boldRed = CellAttributes.DEFAULT
                .withForeground(Color.RED)
                .withStyle(Style.BOLD);
        buffer.setCurrentAttributes(boldRed);
        buffer.setCursor(0, 0);
        buffer.write("Hello, Terminal!");
        System.out.println("  wrote: \"Hello, Terminal!\" with BOLD + RED");
        System.out.println("  row 0: \"" + buffer.getLine(0).stripTrailing() + "\"");

        // ── Step 2: existing content, then green italic insert mid-line ────────
        System.out.println("\n--- Writing existing content on row 2, then inserting mid-line ---");
        buffer.setCurrentAttributes(CellAttributes.DEFAULT);
        buffer.setCursor(2, 0);
        buffer.write("<<BEFORE>><<AFTER>>");
        System.out.println("  wrote: \"<<BEFORE>><<AFTER>>\" at (2, 0) with default attrs");

        CellAttributes greenItalic = CellAttributes.DEFAULT
                .withForeground(Color.GREEN)
                .withStyle(Style.ITALIC);
        buffer.setCurrentAttributes(greenItalic);
        buffer.setCursor(2, 10);  // between >>  <<
        buffer.insert("[INSERTED]");
        System.out.println("  inserted: \"[INSERTED]\" at (2, 10) with GREEN + ITALIC");
        System.out.println("  row 2: \"" + buffer.getLine(2).stripTrailing() + "\"");

        // ── Step 3: fill row 5 with '=' ───────────────────────────────────────
        System.out.println("\n--- Filling row 5 with '=' using BLUE + UNDERLINE ---");
        CellAttributes blueUnderline = CellAttributes.DEFAULT
                .withForeground(Color.BLUE)
                .withStyle(Style.UNDERLINE);
        buffer.setCurrentAttributes(blueUnderline);
        buffer.setCursor(5, 0);
        buffer.fillLine('=');
        System.out.println("  row 5 first char : '" + buffer.getCharAt(5, 0) + "'");
        System.out.println("  row 5 attrs (col 0): fg=" + buffer.getAttributesAt(5, 0).foreground()
                + ", underline=" + buffer.getAttributesAt(5, 0).hasStyle(Style.UNDERLINE));

        // ── Step 4: write 30 lines to trigger scrollback ──────────────────────
        // Fill the 24-row screen first (rows 0–23), then scroll lines 25–30 in.
        System.out.println("\n--- Writing 30 lines (fills screen, then triggers scrollback) ---");
        buffer.clearAll();
        buffer.setCurrentAttributes(CellAttributes.DEFAULT);

        // Fill all 24 screen rows
        for (int i = 1; i <= 24; i++) {
            buffer.setCursor(i - 1, 0);
            buffer.write(String.format("Line %02d", i));
        }
        // Scroll in lines 25–30 (each insertLineAtBottom pushes line 0 to scrollback)
        for (int i = 25; i <= 30; i++) {
            buffer.insertLineAtBottom();
            buffer.setCursor(buffer.getHeight() - 1, 0);
            buffer.write(String.format("Line %02d", i));
        }
        System.out.println("  scrollback size : " + buffer.getScrollbackSize()
                + "  (Lines 01–06 were pushed out)");
        System.out.println("  total rows      : " + buffer.getTotalRows()
                + "  (6 scrollback + 24 screen)");

        // ── Step 5: screen content ────────────────────────────────────────────
        System.out.println("\n--- Screen content (getScreenContent), first and last rows ---");
        String[] screenLines = buffer.getScreenContent().split("\n");
        System.out.println("  screen[0]  : \"" + screenLines[0].stripTrailing() + "\"");
        System.out.println("  screen[23] : \"" + screenLines[23].stripTrailing() + "\"");

        // ── Step 6: full content ──────────────────────────────────────────────
        System.out.println("\n--- Full content (getFullContent), showing rows 0, 5, 6, 29 ---");
        String[] fullLines = buffer.getFullContent().split("\n");
        System.out.printf("  unified[%2d] : \"%s\"%n", 0,  fullLines[0].stripTrailing());
        System.out.printf("  unified[%2d] : \"%s\"%n", 5,  fullLines[5].stripTrailing());
        System.out.printf("  unified[%2d] : \"%s\"%n", 6,  fullLines[6].stripTrailing());
        System.out.printf("  unified[%2d] : \"%s\"%n", 29, fullLines[29].stripTrailing());

        // ── Step 7: unified coordinate system spot-checks ────────────────────
        System.out.println("\n--- Unified coordinate system spot-checks ---");
        int sbSize = buffer.getScrollbackSize();

        // Format is "Line %02d" → col 6 holds the units digit
        // Oldest scrollback line (unified row 0 = scrollback[0])
        System.out.println("  getCharAt(0, 6)             : '"
                + buffer.getCharAt(0, 6) + "'  ← oldest scrollback ('Line 01', col 6 = '1')");

        // Last scrollback line (unified row sbSize-1)
        System.out.println("  getCharAt(" + (sbSize - 1) + ", 6)             : '"
                + buffer.getCharAt(sbSize - 1, 6) + "'  ← newest scrollback ('Line 06', col 6 = '6')");

        // First screen line (unified row sbSize)
        System.out.println("  getCharAt(" + sbSize + ", 6)            : '"
                + buffer.getCharAt(sbSize, 6) + "'  ← first screen row  ('Line 07', col 6 = '7')");

        // Last screen line (unified row sbSize + height - 1)
        int lastRow = sbSize + buffer.getHeight() - 1;
        System.out.println("  getCharAt(" + lastRow + ", 5)           : '"
                + buffer.getCharAt(lastRow, 5) + "'  ← last screen row   ('Line 30', col 5 = '3')");

        // Attribute at first screen row, col 0 — should be DEFAULT (written without special attrs)
        CellAttributes atFirst = buffer.getAttributesAt(sbSize, 0);
        System.out.println("  getAttributesAt(" + sbSize + ", 0).foreground() : "
                + atFirst.foreground() + "  (DEFAULT — no colour was set for line loop)");
        System.out.println("  getAttributesAt(" + sbSize + ", 0).hasStyle(BOLD): "
                + atFirst.hasStyle(Style.BOLD));
    }
}
