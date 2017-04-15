package com.frederikam.gensokyobot.command.music.control;

import com.frederikam.gensokyobot.audio.GuildPlayer;
import com.frederikam.gensokyobot.audio.PlayerRegistry;
import com.frederikam.gensokyobot.commandmeta.abs.Command;
import com.frederikam.gensokyobot.commandmeta.abs.IMusicCommand;
import com.frederikam.gensokyobot.feature.I18n;
import com.frederikam.gensokyobot.util.TextUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Created by napster on 17.03.17.
 * <p>
 * This command allows its user to request a reshuffle of the shuffled playlist
 */
public class ReshuffleCommand extends Command implements IMusicCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, Member invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild);
        if (!player.isShuffle()) {
            TextUtils.replyWithName(channel, invoker, I18n.get(guild).getString("reshufflePlayerNotShuffling"));
            return;
        }
        player.reshuffle();
        channel.sendMessage(I18n.get(guild).getString("reshufflePlaylist")).queue();
    }

    @Override
    public String help(Guild guild) {
        String usage = "{0}{1}\n#";
        return usage + I18n.get(guild).getString("helpReshuffleCommand");
    }

}