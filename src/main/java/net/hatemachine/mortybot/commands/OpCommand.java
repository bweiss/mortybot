package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.Channel;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;
import static net.hatemachine.mortybot.util.IrcUtils.userHasOps;

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
        var user = event.getUser();
        String targetUser = args.isEmpty() ? user.getNick() : args.get(0);

        if (source == PUBLIC) {
            var channel = ((MessageEvent) event).getChannel();
            bot.sendIRC().mode(channel.getName(), "+o " + targetUser);
        } else if (source == PRIVATE) {
            for (Channel channel : bot.getUserChannelDao().getAllChannels()) {
                if (!bot.hasOps(channel)) {
                    log.debug("Bot does not have ops on {}, skipping...", channel.getName());
                } else {
                    if (userHasOps(targetUser, channel)) {
                        log.debug("{} is already an operator on {}", targetUser, channel.getName());
                    } else {
                        log.debug("Setting mode [+o {}] on {}", targetUser, channel.getName());
                        bot.sendIRC().mode(channel.getName(), "+o " + targetUser);
                    }
                }
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
