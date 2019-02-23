package com.bestialMania.sound;

import com.bestialMania.Main;
import com.bestialMania.MemoryManager;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

/**
 * Class representing a sound buffer
 */
public class Sound {
    private int buffer;

    /**
     * Initialize a sound buffer
     */
    public Sound(MemoryManager mm, String fileName) {
        if(!Main.AUDIO) return;
        mm.addSound(this);

        buffer = alGenBuffers();
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
            loadBuffer(ais);
        }catch(Exception e) {
            e.printStackTrace();
            System.err.println("Unable to load sound: " + fileName);
        }
    }

    /**
     * Get the buffer ID
     */
    public int getBuffer() {
        return buffer;
    }

    /**
     * Delete the sound buffer from memory
     */
    public void cleanUp() {
        if(!Main.AUDIO) return;
        alDeleteBuffers(buffer);
    }


    /**
     * Load sound from an audio input stream
     */
    private void loadBuffer(AudioInputStream ais) throws Exception {
        //get format of data
        AudioFormat audioformat = ais.getFormat();

        // get channels
        int format = 0;
        if (audioformat.getChannels() == 1) {
            if (audioformat.getSampleSizeInBits() == 8) {
                format = AL10.AL_FORMAT_MONO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                format = AL10.AL_FORMAT_MONO16;
            } else {
                throw new IllegalStateException("Unsupported sample size");
            }
        } else if (audioformat.getChannels() == 2) {
            if (audioformat.getSampleSizeInBits() == 8) {
                format = AL10.AL_FORMAT_STEREO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                format = AL10.AL_FORMAT_STEREO16;
            } else {
                throw new IllegalStateException("Unsupported sample size");
            }
        } else {
            throw new IllegalStateException("Only Stereo/Mono supported");
        }

        //read data into buffer
        int available = ais.available();
        if(available <= 0) {
            available = ais.getFormat().getChannels() * (int) ais.getFrameLength() * ais.getFormat().getSampleSizeInBits() / 8;
        }
        byte[] buf = new byte[ais.available()];
        int read = 0, total = 0;
        while ((read = ais.read(buf, total, buf.length - total)) != -1
                && total < buf.length) {
            total += read;
        }
        ByteBuffer data = convertAudioBytes(buf, audioformat.getSampleSizeInBits() == 16, audioformat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        alBufferData(this.buffer,format,data,(int)audioformat.getSampleRate());
    }

    /**
     * Convert audio to a ByteBuffer
     */
    private static ByteBuffer convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data, ByteOrder order) {
        ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audio_bytes);
        src.order(order);
        if (two_bytes_data) {
            ShortBuffer dest_short = dest.asShortBuffer();
            ShortBuffer src_short = src.asShortBuffer();
            while (src_short.hasRemaining())
                dest_short.put(src_short.get());
        } else {
            while (src.hasRemaining())
                dest.put(src.get());
        }
        dest.rewind();
        return dest;
    }
}
