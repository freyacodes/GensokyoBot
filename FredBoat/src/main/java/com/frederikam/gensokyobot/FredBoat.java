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
import com.frederikam.gensokyobot.agent.ShardWatchdogAgent;
import com.frederikam.gensokyobot.commandmeta.CommandRegistry;
import com.frederikam.gensokyobot.commandmeta.init.CommandInitializer;
import com.frederikam.gensokyobot.event.EventListenerBoat;
import com.frederikam.gensokyobot.event.ShardWatchdogListener;
import com.frederikam.gensokyobot.feature.I18n;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FredBoat {

    private static final Logger log = LoggerFactory.getLogger(FredBoat.class);

    static final int SHARD_CREATION_SLEEP_INTERVAL = 5100;

    private static final ArrayList<FredBoat> shards = new ArrayList<>();
    public static final long START_TIME = System.currentTimeMillis();
    public static final int UNKNOWN_SHUTDOWN_CODE = -991023;
    public static int shutdownCode = UNKNOWN_SHUTDOWN_CODE;//Used when specifying the intended code for shutdown hooks
    static EventListenerBoat listenerBot;
    ShardWatchdogListener shardWatchdogListener = null;
    private static AtomicInteger numShardsReady = new AtomicInteger(0);

    //unlimited threads = http://i.imgur.com/H3b7H1S.gif
    //use this executor for various small async tasks
    public final static ExecutorService executor = Executors.newCachedThreadPool();

    JDA jda;

    private boolean hasReadiedOnce = false;

    public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, IOException, UnirestException {
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

        //Initialise event listener
        listenerBot = new EventListenerBoat();

        CommandInitializer.initCommands();

        log.info("Loaded commands, registry size is " + CommandRegistry.getSize());

        /* Init JDA */
        initBotShards(listenerBot);

        ShardWatchdogAgent shardWatchdogAgent = new ShardWatchdogAgent();
        shardWatchdogAgent.setDaemon(true);
        shardWatchdogAgent.start();

        if(Config.CONFIG.getStreamUrl().equals(Config.GENSOKYO_RADIO_STREAM_URL)) {
            new GensokyoInfoAgent().start();
        }
    }

    private static void initBotShards(EventListener listener) {
        for(int i = 0; i < Config.CONFIG.getNumShards(); i++){
            try {
                shards.add(i, new FredBoatBot(i, listener));
            } catch (Exception e) {
                log.error("Caught an exception while starting shard " + i + "!", e);
                numShardsReady.getAndIncrement();
            }
            try {
                Thread.sleep(SHARD_CREATION_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException("Got interrupted while setting up bot shards!", e);
            }
        }

        log.info(shards.size() + " shards have been constructed");

    }

    public void onInit(ReadyEvent readyEvent) {
        if (!hasReadiedOnce) {
            numShardsReady.incrementAndGet();
            hasReadiedOnce = false;
        }

        log.info("Received ready event for " + FredBoat.getInstance(readyEvent.getJDA()).getShardInfo().getShardString());

        int ready = numShardsReady.get();
        if (ready == Config.CONFIG.getNumShards()) {
            log.info("All " + ready + " shards are ready.");
        }
    }

    //Shutdown hook
    private static final Runnable ON_SHUTDOWN = () -> {
        int code = shutdownCode != UNKNOWN_SHUTDOWN_CODE ? shutdownCode : -1;

        for(FredBoat fb : shards) {
            fb.getJda().shutdown();
        }

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

    public static EventListenerBoat getListenerBot() {
        return listenerBot;
    }

    /* Sharding */

    public JDA getJda() {
        return jda;
    }

    public static List<FredBoat> getShards() {
        return shards;
    }

    public static List<Guild> getAllGuilds() {
        ArrayList<Guild> list = new ArrayList<>();

        for (FredBoat fb : shards) {
            list.addAll(fb.getJda().getGuilds());
        }

        return list;
    }

    public static Map<String, User> getAllUsersAsMap() {
        HashMap<String, User> map = new HashMap<>();

        for (FredBoat fb : shards) {
            for (User usr : fb.getJda().getUsers()) {
                map.put(usr.getId(), usr);
            }
        }

        return map;
    }

    public static TextChannel getTextChannelById(String id) {
        for (FredBoat fb : shards) {
            for (TextChannel channel : fb.getJda().getTextChannels()) {
                if(channel.getId().equals(id)) return channel;
            }
        }

        return null;
    }

    public static VoiceChannel getVoiceChannelById(String id) {
        for (FredBoat fb : shards) {
            for (VoiceChannel channel : fb.getJda().getVoiceChannels()) {
                if(channel.getId().equals(id)) return channel;
            }
        }

        return null;
    }

    public static FredBoat getInstance(JDA jda) {
        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        for(FredBoat fb : shards) {
            if(((FredBoatBot) fb).getShardId() == sId) {
                return fb;
            }
        }

        throw new IllegalStateException("Attempted to get instance for JDA shard that is not indexed");
    }

    public static FredBoat getInstance(int id) {
        return shards.get(id);
    }

    public static JDA getFirstJDA(){
        return shards.get(0).getJda();
    }

    public ShardInfo getShardInfo() {
        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        if(jda.getAccountType() == AccountType.CLIENT) {
            return new ShardInfo(0, 1);
        } else {
            return new ShardInfo(sId, Config.CONFIG.getNumShards());
        }
    }

    public void revive() {
        jda.shutdown();
        shards.set(getShardInfo().getShardId(), new FredBoatBot(getShardInfo().getShardId(), listenerBot));
    }

    public ShardWatchdogListener getShardWatchdogListener() {
        return shardWatchdogListener;
    }

    @SuppressWarnings("WeakerAccess")
    public class ShardInfo {

        int shardId;
        int shardTotal;

        ShardInfo(int shardId, int shardTotal) {
            this.shardId = shardId;
            this.shardTotal = shardTotal;
        }

        public int getShardId() {
            return this.shardId;
        }

        public int getShardTotal() {
            return this.shardTotal;
        }

        public String getShardString() {
            return String.format("[%02d / %02d]", this.shardId, this.shardTotal);
        }

        @Override
        public String toString() {
            return getShardString();
        }
    }
}
