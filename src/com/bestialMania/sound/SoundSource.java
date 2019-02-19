package com.bestialMania.sound;

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
        source = alGenSources();
        alSourcef(source,AL_PITCH,1.0f);
        alSourcef(source,AL_GAIN,1.0f);
        alSource3f(source,AL_POSITION,0,0,0);
        alSource3f(source,AL_VELOCITY,0,0,0);
        alSourcei(source,AL_LOOPING,loop ? AL_TRUE : AL_FALSE);
        alSourcei(source,AL_BUFFER,sound.getBuffer());
    }

    /**
     * Start playing the sound
     */
    public void play() {
        alSourcePlay(source);
    }

    /**
     * Pause the sound
     */
    public void pause() {
        alSourcePause(source);
    }

    /**
     * Stop the sound
     */
    public void stop() {
        alSourceStop(source);
    }

    /**
     * Returns if the sound is playing
     */
    public boolean isPlaying() {
        return alGetSourcei(source,AL_SOURCE_STATE) == AL_PLAYING;
    }

    /**
     * Delete the sound source
     */
    public void cleanUp() {
        alDeleteSources(source);
    }
}
