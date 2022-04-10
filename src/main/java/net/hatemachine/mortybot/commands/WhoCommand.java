package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.dcc.ChatSession;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.User;
import org.pircbotx.dcc.Chat;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.DCC;

public class WhoCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(WhoCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public WhoCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (source != DCC) {
            event.respondWith("This command is only enabled over DCC chat");
        } else {
            DccManager dccManager = DccManager.getManager();
            List<User> partyLineUsers = dccManager.getActiveChatSessions()
                    .stream()
                    .map(ChatSession::getChat)
                    .map(Chat::getUser)
                    .toList();

            if (partyLineUsers.isEmpty()) {
                String errMessage = "WHO command with no party line users! This should never happen. D'oh!";
                log.error(errMessage);
                throw new RuntimeException(errMessage);
            } else {
                event.respondWith(String.format("There %s %d user%s on the party line: %s",
                        partyLineUsers.size() > 1 ? "are" : "is",
                        partyLineUsers.size(),
                        partyLineUsers.size() > 1 ? "s" : "",
                        partyLineUsers.stream().map(User::getNick).collect(Collectors.joining(", "))));
            }
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
