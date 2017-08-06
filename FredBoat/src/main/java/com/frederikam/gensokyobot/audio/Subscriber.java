package com.frederikam.gensokyobot.audio;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Subscriber {

    private static final Logger log = LoggerFactory.getLogger(Subscriber.class);

    private final StreamCombiner streamCombiner;
    private ConcurrentLinkedDeque<AudioFrame> buffer = new ConcurrentLinkedDeque<>();
    boolean connected = true;

    Subscriber(StreamCombiner streamCombiner) {
        this.streamCombiner = streamCombiner;
        log.info("Sub!");
    }

    void feed(AudioFrame frame) {
        buffer.add(frame);
    }

    public AudioFrame provide() {
        return buffer.poll();
    }

    public int size() {
        return buffer.size();
    }

    public void unsubscribe() {
        streamCombiner.unsubscribe(this);
        connected = false;
        buffer.clear();
        log.info("Unsub!");
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "streamCombiner=" + streamCombiner +
                ", bufferSize=" + buffer.size() +
                ", connected=" + connected +
                '}';
    }
}
