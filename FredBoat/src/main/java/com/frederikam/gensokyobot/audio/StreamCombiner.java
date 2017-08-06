package com.frederikam.gensokyobot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class StreamCombiner extends Thread {

    private static final Logger log = LoggerFactory.getLogger(StreamCombiner.class);

    private final String streamIdentifier;
    private static final int INTERVAL_MILLIS = 16; // 16 millis
    private AudioTrack track;
    private AudioPlayer player;
    private ArrayList<Subscriber> subscribers = new ArrayList<>();

    StreamCombiner(String streamIdentifier) {
        this.streamIdentifier = streamIdentifier;
        player = GuildPlayer.audioPlayerManager.createPlayer();
        player.checkCleanup(Long.MAX_VALUE); //No thanks

        setDaemon(true);

        try {
            track = new AudioLoader().loadSync(streamIdentifier);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        setName("StreamCombiner:" + track.getInfo().title);
    }

    @Override
    public void run() {
        player.startTrack(track, false);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                tick();
                sleep(INTERVAL_MILLIS);
            } catch (Exception e) {
                log.error("Caught an exception while streaming!", e);
                try {
                    sleep(1000);
                } catch (InterruptedException e1) {
                    log.error("Interrupted while sleeping after an exception in the streamer", e);
                    break;
                }
            }
        }
    }

    private void tick() throws InterruptedException {
        if(getAverageBufferSize() > 100) {
            //Buffers have plenty to go on
            return;
        }

        AudioFrame provided = player.provide();

        if(subscribers.isEmpty()) {
            // ¯\_(ツ)_/¯
            return;
        }

        // Make sure we reconnect if we lose connection
        if(player.getPlayingTrack() == null) {
            log.info("Track was somehow skipped. Trying to reload the stream...");
            player.playTrack(new AudioLoader().loadSync(streamIdentifier));
            log.info("Loaded new track: " + player.getPlayingTrack().getInfo().title);
        }

        ArrayList<Subscriber> toUnsubscribe = new ArrayList<>();

        if(provided != null) {
            subscribers.forEach(sub -> {
                if(sub.size() > 50 * 5) {
                    //Do we have 5 seconds of audio? If so let's unsub
                    toUnsubscribe.add(sub);
                    log.warn("Unsubscribed stream for having an abnormally long buffer");
                } else {
                    sub.feed(provided);
                }
            });
        }

        toUnsubscribe.forEach(Subscriber::unsubscribe);
    }

    private int getAverageBufferSize() {
        if(subscribers.size() == 0) return -1;

        final int[] total = {0};
        subscribers.forEach(sub -> total[0] =+ sub.size());
        return total[0] / subscribers.size();
    }

    Subscriber subscribe() {
        Subscriber sub = new Subscriber(this);
        subscribers.add(sub);

        return sub;
    }

    void unsubscribe(Subscriber sub) {
        subscribers.remove(sub);
    }

    private class AudioLoader implements AudioLoadResultHandler {

        private AudioTrack loadedItem;
        private boolean used = false;

        AudioTrack loadSync(String identifier) throws InterruptedException {
            if(used)
                throw new IllegalStateException("This loader can only be used once per instance");

            used = true;

            GuildPlayer.audioPlayerManager.loadItem(identifier, this);

            synchronized (this) {
                this.wait();
            }

            return loadedItem;
        }

        @Override
        public void trackLoaded(AudioTrack audioTrack) {
            loadedItem = audioTrack;
            log.info("Loaded track " + audioTrack.getInfo().title);
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist audioPlaylist) {
            log.error("Loaded playlist instead of stream");
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void noMatches() {
            log.error("No matches");
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void loadFailed(FriendlyException e) {
            log.error("Load failed", e);
            synchronized (this) {
                this.notify();
            }
        }
    }

}
