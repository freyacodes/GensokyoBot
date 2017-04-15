package com.frederikam.gensokyobot.commandmeta.init;

import com.frederikam.gensokyobot.command.admin.BotRestartCommand;
import com.frederikam.gensokyobot.command.admin.CompileCommand;
import com.frederikam.gensokyobot.command.admin.EvalCommand;
import com.frederikam.gensokyobot.command.admin.ExitCommand;
import com.frederikam.gensokyobot.command.admin.ReviveCommand;
import com.frederikam.gensokyobot.command.admin.UpdateCommand;
import com.frederikam.gensokyobot.command.maintenance.StatsCommand;
import com.frederikam.gensokyobot.command.music.control.JoinCommand;
import com.frederikam.gensokyobot.command.music.control.LeaveCommand;
import com.frederikam.gensokyobot.command.music.info.NowplayingCommand;
import com.frederikam.gensokyobot.command.util.HelpCommand;
import com.frederikam.gensokyobot.commandmeta.CommandRegistry;

public class CommandInitializer {

    public static void initCommands() {
        CommandRegistry.registerCommand("help", new HelpCommand());
        CommandRegistry.registerAlias("help", "commands");

        CommandRegistry.registerCommand("join", new JoinCommand());
        CommandRegistry.registerAlias("join", "play");
        CommandRegistry.registerCommand("leave", new LeaveCommand());
        CommandRegistry.registerAlias("leave", "stop");
        CommandRegistry.registerCommand("stats", new StatsCommand());
        CommandRegistry.registerCommand("np", new NowplayingCommand());
        CommandRegistry.registerAlias("np", "nowplaying");

        //Admin commands
        CommandRegistry.registerCommand("restart", new BotRestartCommand());
        CommandRegistry.registerCommand("compile", new CompileCommand());
        CommandRegistry.registerCommand("eval", new EvalCommand());
        CommandRegistry.registerCommand("exit", new ExitCommand());
        CommandRegistry.registerCommand("revive", new ReviveCommand());
        CommandRegistry.registerCommand("update", new UpdateCommand());
    }

}
