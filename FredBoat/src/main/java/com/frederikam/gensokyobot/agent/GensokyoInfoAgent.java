package com.frederikam.gensokyobot.agent;

import com.frederikam.gensokyobot.FredBoat;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GensokyoInfoAgent extends Thread {

    private static final Logger log = LoggerFactory.getLogger(GensokyoInfoAgent.class);

    private static final int INTERVAL_MILLIS = 5000; // 5 secs
    private static String info = null;
    private static String lastSong = "";

    public GensokyoInfoAgent() {
        setDaemon(true);
        setName("GensokyoInfoAgent");
    }

    @Override
    public void run() {
        log.info("Started GensokyoInfoAgent");

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                fetch();
                sleep(INTERVAL_MILLIS);
            } catch (Exception e) {
                log.error("Caught an exception while fetching info!", e);
                try {
                    sleep(1000);
                } catch (InterruptedException e1) {
                    log.error("Interrupted while sleeping after an exception in the agent", e);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private static String fetch() {
        try {
            info = Unirest.get("https://gensokyoradio.net/xml").asString().getBody();

            JSONObject data = XML.toJSONObject(GensokyoInfoAgent.getInfo()).getJSONObject("GENSOKYORADIODATA");

            String newSong = data.getJSONObject("SONGINFO").getString("TITLE");

            if (!newSong.equals(lastSong)) {
                List<FredBoat> shards = FredBoat.getShards();
                for(FredBoat shard : shards) {
                    shard.getJda().getPresence().setGame(new GameImpl(newSong, null, Game.GameType.DEFAULT));
                }

                log.info("Now playing " + newSong);
            }

            lastSong = data.getJSONObject("SONGINFO").getString("TITLE");

            return info;
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getInfo() {
        return info == null ? fetch() : info;
    }

}
