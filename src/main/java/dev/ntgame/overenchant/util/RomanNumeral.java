package dev.ntgame.overenchant.util;

/** Converts a positive level into a roman numeral for chat/action-bar messages. */
public final class RomanNumeral {

    private static final int[] VALUES = {50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"L", "XL", "X", "IX", "V", "IV", "I"};

    private RomanNumeral() {
    }

    public static String of(int level) {
        if (level <= 0) return String.valueOf(level);
        StringBuilder sb = new StringBuilder();
        int remaining = level;
        for (int i = 0; i < VALUES.length; i++) {
            while (remaining >= VALUES[i]) {
                remaining -= VALUES[i];
                sb.append(SYMBOLS[i]);
            }
        }
        return sb.toString();
    }
}
