package net.hatemachine.mortybot.util;

public class ProgressBar {

    private static final char FILLED_CHAR_DEFAULT = '▓';
    private static final char UNFILLED_CHAR_DEFAULT = '░';

    private final int size;
    private final double unitVal;
    private final char filledChar;
    private final char unfilledChar;
    private final boolean showPercentageFlag;

    public ProgressBar(int size) {
        this(size, FILLED_CHAR_DEFAULT, UNFILLED_CHAR_DEFAULT, false);
    }

    public ProgressBar(int size, boolean showPercentageFlag) {
        this(size, FILLED_CHAR_DEFAULT, UNFILLED_CHAR_DEFAULT, showPercentageFlag);
    }

    public ProgressBar(int size, char filledChar, char unfilledChar) {
        this(size, filledChar, unfilledChar, false);
    }

    public ProgressBar(int size, char filledChar, char unfilledChar, boolean showPercentageFlag) {
        if (size > 100)
            throw new IllegalArgumentException("size cannot exceed 100");

        this.size = size;
        this.unitVal = 100.0 / size;
        this.filledChar = filledChar;
        this.unfilledChar = unfilledChar;
        this.showPercentageFlag = showPercentageFlag;
    }

    public String show(double percent) {
        StringBuilder bar = new StringBuilder();

        for (double cnt = unitVal; cnt <= percent; cnt = cnt + unitVal) {
            bar.append(filledChar);
        }

        while (bar.length() < size) {
            bar.append(unfilledChar);
        }

        if (showPercentageFlag) {
            bar.append(" ").append((int) percent).append("%");
        }

        return bar.toString();
    }

    public String show(double progress, double complete) {
        if (progress > complete)
            throw new IllegalArgumentException("progress cannot exceed complete");

        double percent = (progress / complete) * 100;
        return this.show(percent);
    }
}
