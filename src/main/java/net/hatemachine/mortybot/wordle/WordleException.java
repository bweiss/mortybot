package net.hatemachine.mortybot.wordle;

public class WordleException extends Exception {
    protected final Reason reason;

    public enum Reason {
        WORD_INVALID
    }

    public WordleException(Reason reason, String message) {
        this(reason, message, null);
    }

    public WordleException(Reason reason, String message, Exception e) {
        super(reason + ": " + message, e);
        this.reason = reason;
    }

    public WordleException.Reason getReason() {
        return this.reason;
    }
}
