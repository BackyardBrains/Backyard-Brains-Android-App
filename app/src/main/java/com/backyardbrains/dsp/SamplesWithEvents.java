package com.backyardbrains.dsp;

import com.backyardbrains.utils.EventUtils;

/**
 * Data holder class that hold samples, events and last sample index after processing.
 *
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
public class SamplesWithEvents {

    private static final int EVENT_BUFFER_SIZE = EventUtils.MAX_EVENT_COUNT;

    public int channelCount;
    public int frameCount;

    public short[] samples;
    public int sampleCount;
    public short[][] samplesM;
    public int[] sampleCountM;
    public int[] eventIndices;
    public String[] eventNames;
    public int eventCount;
    public long lastSampleIndex = -1;

    public SamplesWithEvents(int channelCount, int frameCount) {
        this.channelCount = channelCount;
        this.frameCount = frameCount;

        this.samples = new short[channelCount * frameCount];
        this.sampleCount = 0;
        this.samplesM = new short[channelCount][];
        for (int i = 0; i < channelCount; i++) {
            this.samplesM[i] = new short[frameCount];
        }
        this.sampleCountM = new int[channelCount];
        this.eventIndices = new int[EVENT_BUFFER_SIZE];
        this.eventNames = new String[EVENT_BUFFER_SIZE];
        this.eventCount = 0;
    }
}
