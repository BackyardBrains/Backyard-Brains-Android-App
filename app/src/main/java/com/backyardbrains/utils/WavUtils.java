package com.backyardbrains.utils;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

/**
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
public class WavUtils {

    /**
     * WAV header size
     */
    public static final int HEADER_SIZE = 44;

    /**
     * Converts specified {@code sampleCount} to wav time progress and returns it formatted as {@code mm:ss}.
     */
    public static String formatWavProgress(int sampleCount, int sampleRate, int channelCount,
        int bitsPerSample) {
        long byteCount = AudioUtils.getByteCount(sampleCount, bitsPerSample);
        byteCount -= HEADER_SIZE;

        return Formats.formatTime_mm_ss(TimeUnit.SECONDS.toMillis(
            (long) toSeconds(byteCount, sampleRate, channelCount, bitsPerSample)));
    }

    /**
     * Returns length of the wav file of specified {@code byteCount} length formatted as "XX s" or
     * "XX m XX s".
     */
    public static CharSequence formatWavLength(float seconds) {
        return Formats.formatTime_m_s((long) seconds);
    }

    public static byte[] writeHeader(long totalAudioLength, int sampleRateInHz, int channelCount,
        int audioFormat) {
        final byte bitsPerSample = (byte) AudioUtils.getBitsPerSample(audioFormat);
        return writeHeader(totalAudioLength - HEADER_SIZE, totalAudioLength - HEADER_SIZE + 36,
            sampleRateInHz, channelCount, bitsPerSample * sampleRateInHz * channelCount / 8,
            bitsPerSample);
    }

    /**
     * Reads a header of the specified {@code wavStream}.
     *
     * @throws IOException if specified stream is not a WAV stream
     */
    public static WavHeader readHeader(@NonNull InputStream wavStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        //noinspection ResultOfMethodCallIgnored
        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();

        // fast-forward to audio format
        buffer.position(buffer.position() + 20);
        // audio format
        int format = buffer.getShort();
        // PCM = 1 (i.e. Linear quantization) Values other than 1 indicate some form of compression.
        check(format == 1 || format == 3,
            "Unsupported audio format: " + format); // 1 means PCM, 2 means FLOATS
        // number of channels
        int channels = buffer.getShort();
        // Mono = 1, Stereo = 2, etc.
        check(channels > 0, "Unsupported number of channels: " + channels);
        // sample rate
        int rate = buffer.getInt();
        // 8000, 44100, etc. (for now we support only 5000, 10000 and 44100)
        check(
            rate == AudioUtils.DEFAULT_SAMPLE_RATE || rate == SampleStreamUtils.DEFAULT_SAMPLE_RATE
                || rate == SampleStreamUtils.SAMPLE_RATE_5000, "Unsupported sample rate: " + rate);

        // fast-forward to bits per sample
        buffer.position(buffer.position() + 6);
        // bits per sample
        int bits = buffer.getShort();
        check(bits == 16 || bits == 32, "Unsupported number of bits per sample: " + bits);
        // data
        int dataSize;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            int size = buffer.getInt();
            //noinspection ResultOfMethodCallIgnored
            wavStream.skip(size);

            buffer.rewind();
            //noinspection ResultOfMethodCallIgnored
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        check(dataSize > 0, "Wrong data size: " + dataSize);

        return new WavHeader(channels, rate, bits, dataSize);
    }
    //bitrate = bitsPerSample * samplesPerSecond * channels;
    //fileSize = (bitsPerSample * samplesPerSecond * channels * duration) / 8;

    // Converts specified byteCount to seconds
    private static float toSeconds(long byteCount, int sampleRate, int channelCount,
        int bitsPerSample) {
        return byteCount / (float) (sampleRate * channelCount * bitsPerSample / 8);
    }

    // Writes and returns WAV header following specified parameters
    private static byte[] writeHeader(long totalAudioLen, long totalDataLen, long sampleRate,
        int channelCount, long byteRate, byte bitsPerSample) {

        byte[] header = new byte[HEADER_SIZE];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channelCount;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channelCount * (bitsPerSample / 8)); // block align
        header[33] = 0;
        header[34] = bitsPerSample; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

    // Convenience method that throws IOException with specified message if assertion is false
    private static void check(boolean assertion, String message) throws IOException {
        if (!assertion) throw new IOException(message);
    }

    /**
     * VO that holds information for a single WAV file.
     */
    public static class WavHeader {
        private final int numChannels;
        private final int sampleRate;
        private final int bitsPerSample;
        private final int dataSize;

        WavHeader(int numChannels, int rate, int bitsPerSample, int dataSize) {
            this.numChannels = numChannels;
            this.sampleRate = rate;
            this.bitsPerSample = bitsPerSample;
            this.dataSize = dataSize;
        }

        public int getChannelCount() {
            return numChannels;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public int getBitsPerSample() {
            return bitsPerSample;
        }

        public int getDataSize() {
            return dataSize;
        }
    }
}
