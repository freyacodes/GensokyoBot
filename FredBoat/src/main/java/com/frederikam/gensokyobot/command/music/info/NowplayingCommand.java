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

package com.frederikam.gensokyobot.command.music.info;

import com.frederikam.gensokyobot.Config;
import com.frederikam.gensokyobot.agent.GensokyoInfoAgent;
import com.frederikam.gensokyobot.commandmeta.abs.Command;
import com.frederikam.gensokyobot.commandmeta.abs.IMusicCommand;
import com.frederikam.gensokyobot.feature.I18n;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONObject;
import org.json.XML;

import java.awt.*;
import java.text.MessageFormat;

public class NowplayingCommand extends Command implements IMusicCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {

        if(!Config.CONFIG.getStreamUrl().equals(Config.GENSOKYO_RADIO_STREAM_URL)) {
            channel.sendMessage("Info unavailable for this stream").queue();
            return;
        }

        sendGensokyoRadioEmbed(channel);
    }

    static void sendGensokyoRadioEmbed(TextChannel channel) {
        JSONObject data = XML.toJSONObject(GensokyoInfoAgent.getInfo()).getJSONObject("GENSOKYORADIODATA");

        String rating = data.getJSONObject("MISC").getInt("TIMESRATED") == 0 ?
                I18n.get(channel.getGuild()).getString("noneYet") :
                MessageFormat.format(I18n.get(channel.getGuild()).getString("npRatingRange"), data.getJSONObject("MISC").getInt("RATING"), data.getJSONObject("MISC").getInt("TIMESRATED"));

        String albumArt = data.getJSONObject("MISC").getString("ALBUMART").equals("") ?
                "https://i.imgur.com/a1SQKuw.png" :
                "https://gensokyoradio.net/images/albums/original/" + data.getJSONObject("MISC").getString("ALBUMART");

        String titleUrl = data.getJSONObject("MISC").getString("CIRCLELINK").equals("") ?
                "https://gensokyoradio.net/" :
                data.getJSONObject("MISC").getString("CIRCLELINK");

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(data.getJSONObject("SONGINFO").getString("TITLE"), titleUrl)
                .addField(I18n.get(channel.getGuild()).getString("album"), data.getJSONObject("SONGINFO").getString("ALBUM"), true)
                .addField(I18n.get(channel.getGuild()).getString("artist"), data.getJSONObject("SONGINFO").getString("ARTIST"), true)
                .addField(I18n.get(channel.getGuild()).getString("circle"), data.getJSONObject("SONGINFO").getString("CIRCLE"), true);

        if (data.getJSONObject("SONGINFO").optInt("YEAR") != 0) {
            eb.addField(I18n.get(channel.getGuild()).getString("year"), Integer.toString(data.getJSONObject("SONGINFO").getInt("YEAR")), true);
        }

        eb.addField(I18n.get(channel.getGuild()).getString("rating"), rating, true)
                .addField(I18n.get(channel.getGuild()).getString("listeners"), Integer.toString(data.getJSONObject("SERVERINFO").getInt("LISTENERS")), true)
                .setImage(albumArt)
                .setColor(new Color(66, 16, 80))
                .setFooter("Content provided by gensokyoradio.net. The GR logo is a trademark of Gensokyo Radio. Gensokyo Radio is © LunarSpotlight.", channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public String help(Guild guild) {
        String usage = "{0}{1}\n#";
        return usage + I18n.get(guild).getString("helpNowplayingCommand");
    }
}
