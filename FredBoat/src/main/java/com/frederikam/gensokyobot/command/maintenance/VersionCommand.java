package com.frederikam.gensokyobot.command.maintenance;

import com.frederikam.gensokyobot.commandmeta.abs.Command;
import com.frederikam.gensokyobot.commandmeta.abs.IMaintenanceCommand;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;


public class VersionCommand extends Command implements IMaintenanceCommand {


    @Override
    public void onInvoke(Guild guild,  TextChannel channel, Member invoker, Message message, String[] args) {
        channel.sendMessage("JDA Version: " + JDAInfo.VERSION);
    }

    @Override
    public String help(Guild guild) {
        return "{0}{1}\n#Show the JDA version.";
    }
}