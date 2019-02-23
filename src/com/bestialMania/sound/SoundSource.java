package com.bestialMania.sound;

import com.bestialMania.Main;

import static org.lwjgl.openal.AL10.*;

/**
 * Playable sound with various properties
 */
public class SoundSource {
    private int source;

    /**
     * Create a sound source with no modifications other than specifying if the sound should loop
     */
    public SoundSource(Sound sound, boolean loop) {
        this(sound,loop,1.0f);
    }
    /**
     * Create a sound source with loop and gain specifications
     */
    public SoundSource(Sound sound, boolean loop,float gain) {
        if(!Main.AUDIO) return;
        source = alGenSources();
        alSourcef(source,AL_PITCH,1.0f);
        alSourcef(source,AL_GAIN,gain);
        alSource3f(source,AL_POSITION,0,0,0);
        alSource3f(source,AL_VELOCITY,0,0,0);
        alSourcei(source,AL_LOOPING,loop ? AL_TRUE : AL_FALSE);
        alSourcei(source,AL_BUFFER,sound.getBuffer());
    }

    /**
     * Start playing the sound
     */
    public void play() {

        if(!Main.AUDIO) return;
        alSourcePlay(source);
    }

    /**
     * Pause the sound
     */
    public void pause() {
        if(!Main.AUDIO) return;
        alSourcePause(source);
    }

    /**
     * Stop the sound
     */
    public void stop() {

        if(!Main.AUDIO) return;
        alSourceStop(source);
    }

    /**
     * Returns if the sound is playing
     */
    public boolean isPlaying() {

        if(!Main.AUDIO) return false;

        return alGetSourcei(source,AL_SOURCE_STATE) == AL_PLAYING;
    }

    /**
     * Delete the sound source
     */
    public void cleanUp() {
        if(!Main.AUDIO) return;
        alDeleteSources(source);
    }
}
