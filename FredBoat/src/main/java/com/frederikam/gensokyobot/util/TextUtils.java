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

package com.frederikam.gensokyobot.util;

import com.frederikam.gensokyobot.commandmeta.MessagingException;
import com.frederikam.gensokyobot.feature.I18n;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TextUtils.class);

    public static Message prefaceWithName(Member member, String msg) {
        msg = ensureSpace(msg);

        MessageBuilder builder = new MessageBuilder().append(member.getEffectiveName()).append(": ").append(msg);
        return builder.build();
    }

    private static String ensureSpace(String msg){
        return msg.charAt(0) == ' ' ? msg : " " + msg;
    }

    public static void handleException(Throwable e, TextChannel channel, Member invoker) {
        if (e instanceof MessagingException) {
            channel.sendMessage(invoker.getEffectiveName() + ": " + e.getMessage()).queue();
            return;
        }

        log.error("Caught exception while executing a command", e);

        MessageBuilder builder = new MessageBuilder();

        if (invoker != null) {
            builder.append(invoker);

            String filtered = MessageFormat.format(I18n.get().getString("utilErrorOccurred"), e.toString());

            builder.append(filtered);
        } else {
            String filtered = MessageFormat.format(I18n.DEFAULT.getProps().getString("utilErrorOccurred"), e.toString());

            builder.append(filtered);
        }

        //builder.append("```java\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.append("\t" + ste.toString() + "\n");
            if ("prefixCalled".equals(ste.getMethodName())) {
                break;
            }
        }
        builder.append("\t...```");

        Message out = builder.build();

        try {
            channel.sendMessage(out).queue();
        } catch (UnsupportedOperationException tooLongEx) {
            try {channel.sendMessage(MessageFormat.format(I18n.get().getString("errorOccurredTooLong"), postToHastebin(out.getContentRaw()))).queue();
            } catch (UnirestException e1) {
                channel.sendMessage(I18n.get().getString("errorOccurredTooLongAndUnirestException")).queue();
            }
        }
    }

    public static String postToHastebin(String body) throws UnirestException {
        return Unirest.post("https://hastebin.com/documents").body(body).asJson().getBody().getObject().getString("key");
    }
}
