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
package com.frederikam.gensokyobot.commandmeta.init;

import com.frederikam.gensokyobot.command.admin.BotRestartCommand;
import com.frederikam.gensokyobot.command.admin.CompileCommand;
import com.frederikam.gensokyobot.command.admin.EvalCommand;
import com.frederikam.gensokyobot.command.admin.ExitCommand;
import com.frederikam.gensokyobot.command.admin.ReviveCommand;
import com.frederikam.gensokyobot.command.admin.UpdateCommand;
import com.frederikam.gensokyobot.command.maintenance.ShardsCommand;
import com.frederikam.gensokyobot.command.maintenance.StatsCommand;
import com.frederikam.gensokyobot.command.maintenance.VersionCommand;
import com.frederikam.gensokyobot.command.music.control.JoinCommand;
import com.frederikam.gensokyobot.command.music.control.LeaveCommand;
import com.frederikam.gensokyobot.command.music.info.NowplayingCommand;
import com.frederikam.gensokyobot.command.util.HelloCommand;
import com.frederikam.gensokyobot.command.util.HelpCommand;
import com.frederikam.gensokyobot.command.util.InviteCommand;
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
        CommandRegistry.registerCommand("shards", new ShardsCommand());
        CommandRegistry.registerCommand("invite", new InviteCommand());
        CommandRegistry.registerCommand("version", new VersionCommand());
        CommandRegistry.registerCommand("hello", new HelloCommand());
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
