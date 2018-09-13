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

package com.frederikam.gensokyobot;

import com.frederikam.gensokyobot.agent.GensokyoInfoAgent;
import com.frederikam.gensokyobot.commandmeta.CommandRegistry;
import com.frederikam.gensokyobot.commandmeta.init.CommandInitializer;
import com.frederikam.gensokyobot.event.EventListenerBoat;
import com.frederikam.gensokyobot.event.EventLogger;
import com.frederikam.gensokyobot.feature.I18n;
import com.mashape.unirest.http.Unirest;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FredBoat {

    private static final Logger log = LoggerFactory.getLogger(FredBoat.class);

    public static final long START_TIME = System.currentTimeMillis();
    public static final int UNKNOWN_SHUTDOWN_CODE = -991023;
    public static int shutdownCode = UNKNOWN_SHUTDOWN_CODE;//Used when specifying the intended code for shutdown hooks
    private static AtomicInteger numShardsReady = new AtomicInteger(0);

    //unlimited threads = http://i.imgur.com/H3b7H1S.gif
    //use this executor for various small async tasks
    public final static ExecutorService executor = Executors.newCachedThreadPool();

    private static ShardManager shardManager;

    public static void main(String[] args) throws LoginException, IllegalArgumentException, IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(ON_SHUTDOWN));

        log.info("\n\n" +
                " _____                      _               ______       _   \n" +
                "|  __ \\                    | |              | ___ \\     | |  \n" +
                "| |  \\/ ___ _ __  ___  ___ | | ___   _  ___ | |_/ / ___ | |_ \n" +
                "| | __ / _ \\ '_ \\/ __|/ _ \\| |/ / | | |/ _ \\| ___ \\/ _ \\| __|\n" +
                "| |_\\ \\  __/ | | \\__ \\ (_) |   <| |_| | (_) | |_/ / (_) | |_ \n" +
                " \\____/\\___|_| |_|___/\\___/|_|\\_\\\\__, |\\___/\\____/ \\___/ \\__|\n" +
                "                                  __/ |                      \n" +
                "                                 |___/                       " + "\n\n");

        I18n.start();

        int scope;
        try {
            scope = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            log.info("Invalid scope, defaulting to scopes 0x111");
            scope = 0x111;
        }

        log.info("Starting with scopes:"
                + "\n\tMain: " + ((scope & 0x100) == 0x100)
                + "\n\tMusic: " + ((scope & 0x010) == 0x010)
                + "\n\tSelf: " + ((scope & 0x001) == 0x001));

        log.info("JDA version:\t" + JDAInfo.VERSION);

        Config.loadDefaultConfig(scope);

        CommandInitializer.initCommands();

        log.info("Loaded commands, registry size is " + CommandRegistry.getSize());

        DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder()
                .addEventListeners(new EventLogger("216689009110417408"), new EventListenerBoat())
                .setToken(Config.CONFIG.getToken())
                .setBulkDeleteSplittingEnabled(true)
                .setEnableShutdownHook(false)
                .setShardsTotal(Config.CONFIG.getNumShards())
                .setAutoReconnect(true);

        if (!System.getProperty("os.arch").equalsIgnoreCase("arm")
                && !System.getProperty("os.arch").equalsIgnoreCase("arm-linux")
                && !System.getProperty("os.arch").equalsIgnoreCase("darwin")
                && !System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
            builder.setAudioSendFactory(new NativeAudioSendFactory());
        }

        shardManager = builder.build();

        if(Config.CONFIG.getStreamUrl().equals(Config.GENSOKYO_RADIO_STREAM_URL)) {
            new GensokyoInfoAgent().start();
        }
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static void onInit(ReadyEvent readyEvent) {
        numShardsReady.incrementAndGet();

        log.info("Received ready event for " + readyEvent.getJDA().getShardInfo().getShardString());

        int ready = numShardsReady.get();
        if (ready == Config.CONFIG.getNumShards()) {
            log.info("All " + ready + " shards are ready.");
        }
    }

    //Shutdown hook
    private static final Runnable ON_SHUTDOWN = () -> {
        shardManager.shutdown();

        try {
            Unirest.shutdown();
        } catch (IOException ignored) {}

        executor.shutdown();
    };

    public static void shutdown(int code) {
        log.info("Shutting down with exit code " + code);
        shutdownCode = code;

        System.exit(code);
    }
}
