package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;

public class OpCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(OpCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public OpCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        UserChannelDao<User, Channel> dao = bot.getUserChannelDao();
        User targetUser = args.isEmpty() ? event.getUser() : dao.getUser(args.get(0));
        Set<Channel> targetChannels = new HashSet<>();

        if (source == PUBLIC) {
            targetChannels.add(((MessageEvent) event).getChannel());
        } else if (source == PRIVATE) {
            targetChannels.addAll(dao.getChannels(targetUser));
        }

        for (Channel chan : targetChannels) {
            if (chan.isOp(bot.getUserBot()) && !chan.isOp(targetUser)) {
                log.info("Setting mode [+o {}] on {}", targetUser.getNick(), chan.getName());
                bot.sendIRC().mode(chan.getName(), "+o " + targetUser.getNick());
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
