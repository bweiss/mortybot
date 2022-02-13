/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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

public class ProgressBar {

    private static final char FILLED_CHAR_DEFAULT = '\u2593';
    private static final char UNFILLED_CHAR_DEFAULT = '\u2591';

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
