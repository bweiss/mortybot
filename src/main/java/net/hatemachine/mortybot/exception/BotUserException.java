package net.hatemachine.mortybot.exception;

public class BotUserException extends RuntimeException {
    protected final Reason reason;

    public enum Reason {
        BOT_USER_EXISTS,
        UNKNOWN_BOT_USER
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
