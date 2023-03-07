/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.util;

/**
 * A class to represent an ASCII progress bar that can vary in size.
 */
public class ProgressBar {

    private static final char FILLED_CHAR_DEFAULT = '\u2593';
    private static final char UNFILLED_CHAR_DEFAULT = '\u2591';

    private final int size;
    private final char filledChar;
    private final char unfilledChar;
    private final boolean showPercentageFlag;

    /**
     * Creates a progress bar of a certain size with the default fill characters.
     *
     * @param size the total size of the progress bar in characters
     */
    public ProgressBar(int size) {
        this(size, FILLED_CHAR_DEFAULT, UNFILLED_CHAR_DEFAULT);
    }

    /**
     * Creates a progress bar of a certain size with the default fill characters.
     *
     * @param size the total size of the progress bar in characters
     * @param showPercentageFlag if true the percentage will be included after the progress bar
     */
    public ProgressBar(int size, boolean showPercentageFlag) {
        this(size, FILLED_CHAR_DEFAULT, UNFILLED_CHAR_DEFAULT, showPercentageFlag);
    }

    /**
     * Creates a progress bar of a certain size with the specified fill characters.
     *
     * @param size the total size of the progress bar in characters
     * @param filledChar the character used to represent a filled unit of the bar
     * @param unfilledChar the character used to represent an unfilled unit of the bar
     */
    public ProgressBar(int size, char filledChar, char unfilledChar) {
        this(size, filledChar, unfilledChar, false);
    }

    /**
     * Creates a progress bar of a certain size with the specified fill characters.
     *
     * @param size the total size of the progress bar in characters
     * @param filledChar the character used to represent a filled unit of the bar
     * @param unfilledChar the character used to represent an unfilled unit of the bar
     * @param showPercentageFlag if true the percentage will be included after the progress bar
     */
    public ProgressBar(int size, char filledChar, char unfilledChar, boolean showPercentageFlag) {
        if (size > 100) {
            throw new IllegalArgumentException("size cannot exceed 100");
        }

        this.size = size;
        this.filledChar = filledChar;
        this.unfilledChar = unfilledChar;
        this.showPercentageFlag = showPercentageFlag;
    }

    /**
     * Shows an ASCII representation of the progress bar with the given percentage.
     *
     * @param percent the percent filled
     * @return a string representing the progress bar
     */
    public String show(double percent) {
        if (percent > 100) {
            throw new IllegalArgumentException("percent cannot exceed 100");
        }

        StringBuilder bar = new StringBuilder();
        int filled = calculateFilledUnits(percent);
        bar.append(String.valueOf(filledChar).repeat(filled));
        bar.append(String.valueOf(unfilledChar).repeat(size - filled));

        if (showPercentageFlag) {
            bar.append(" ").append((int) percent).append("%");
        }

        return bar.toString();
    }

    /**
     * Shows an ASCII representation of the progress bar based on the given values.
     *
     * @param progress the current progress to display
     * @param complete the value representing a completed bar
     * @return a string representing the progress bar
     */
    public String show(double progress, double complete) {
        if (progress > complete) {
            throw new IllegalArgumentException("progress cannot exceed complete");
        }

        double percent = (progress / complete) * 100;
        return this.show(percent);
    }

    /**
     * Calculates the number of filled units for this bar based on the given percentage.
     * This will round the number of filled units to the nearest integer.
     *
     * @param percent the completion percentage to be represented
     * @return the number of units to fill for this bar, rounded to the nearest whole number
     */
    private int calculateFilledUnits(double percent) {
        return (int) Math.rint(size * (percent / 100));
    }
}
