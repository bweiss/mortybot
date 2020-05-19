package net.hatemachine.mortybot;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class Utilities
{
    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }
}
