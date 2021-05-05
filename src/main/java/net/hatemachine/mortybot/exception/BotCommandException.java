package net.hatemachine.mortybot.exception;

public class BotCommandException extends RuntimeException {
    protected final BotCommandException.Reason reason;

    public enum Reason {
        COMMAND_NOT_ENABLED,
        USER_UNAUTHORIZED
    }

    public BotCommandException(BotCommandException.Reason reason, String message) {
        this(reason, message, null);
    }

    public BotCommandException(BotCommandException.Reason reason, String message, Exception e) {
        super(reason + ": " + message, e);
        this.reason = reason;
    }

    public BotCommandException.Reason getReason() {
        return this.reason;
    }
}
