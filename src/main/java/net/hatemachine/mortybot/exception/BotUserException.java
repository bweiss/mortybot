package net.hatemachine.mortybot.exception;

public class BotUserException extends Exception {
    protected final Reason reason;

    public enum Reason {
        UNKNOWN_USER,
        USER_EXISTS
    }

    public BotUserException(Reason reason, String message) {
        this(reason, message, null);
    }

    public BotUserException(Reason reason, String message, Exception e) {
        super(reason + ": " + message, e);
        this.reason = reason;
    }

    public BotUserException.Reason getReason() {
        return this.reason;
    }
}
