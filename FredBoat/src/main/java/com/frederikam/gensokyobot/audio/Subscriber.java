package com.frederikam.gensokyobot.audio;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Subscriber {

    private final StreamCombiner streamCombiner;
    private ConcurrentLinkedDeque<AudioFrame> buffer = new ConcurrentLinkedDeque<>();
    boolean connected = true;

    Subscriber(StreamCombiner streamCombiner) {
        this.streamCombiner = streamCombiner;
    }

    void feed(AudioFrame frame) {
        buffer.add(frame);
    }

    public AudioFrame provide() {
        return buffer.pollLast();
    }

    public int size() {
        return buffer.size();
    }

    public void unsubscribe() {
        streamCombiner.unsubscribe(this);
    }

    public boolean isConnected() {
        return connected;
    }
}