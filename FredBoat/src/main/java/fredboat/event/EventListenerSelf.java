/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package fredboat.event;

import fredboat.Config;
import fredboat.commandmeta.CommandManager;
import fredboat.commandmeta.CommandRegistry;
import fredboat.commandmeta.abs.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

public class EventListenerSelf extends AbstractEventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListenerSelf.class);

    public EventListenerSelf() {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }

        if (event.getMessage().getContent().length() < Config.CONFIG.getPrefix().length()) {
            return;
        }

        if (event.getMessage().getContent().substring(0, Config.CONFIG.getPrefix().length()).equals(Config.CONFIG.getPrefix())) {
            Command invoked = null;
            try {
                log.info(event.getGuild().getName() + " \t " + event.getAuthor().getName() + " \t " + event.getMessage().getRawContent());
                Matcher matcher = COMMAND_NAME_PREFIX.matcher(event.getMessage().getContent());
                matcher.find();

                invoked = CommandRegistry.getCommand(matcher.group()).command;
            } catch (NullPointerException ignored) {

            }

            if (invoked == null) {
                return;
            }

            CommandManager.prefixCalled(invoked, event.getGuild(), event.getTextChannel(), event.getMember(), event.getMessage());

            try {
                event.getMessage().delete().queue();
            } catch (Exception ignored) {
            }

        }
    }

}
