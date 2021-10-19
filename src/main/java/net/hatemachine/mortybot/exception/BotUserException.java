package net.hatemachine.mortybot.exception;

public class BotUserException extends RuntimeException {
    protected final Reason reason;

    public enum Reason {
        HOSTMASK_EXISTS,
        HOSTMASK_NOT_FOUND,
        USER_EXISTS,
        USER_NOT_FOUND
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
