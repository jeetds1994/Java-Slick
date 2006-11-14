package org.newdawn.slick;

import org.newdawn.slick.openal.InternalSound;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.util.Log;

/**
 * A piece of music loaded and playable within the game
 *
 * @author kevin
 */
public class Music {
	/** The music currently being played or null if none */
	private static Music currentMusic;
	/** The sound from FECK representing this music */
	private InternalSound sound;
	/** True if the music is playing */
	private boolean playing;
	
	/**
	 * Create and load a piece of music (either OGG or MOD/XM)
	 * 
	 * @param ref The location of the music
	 * @throws SlickException
	 */
	public Music(String ref) throws SlickException {
		SoundStore.get().init();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				sound = SoundStore.get().getOgg(ref);
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore.get().getWAV(ref);
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore.get().getMOD(ref);
			} else {
				throw new SlickException("Only .xm, .mod and .ogg are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+ref);
		}
	}

	/**
	 * Loop the music
	 */
	public void loop() {
		loop(1.0f,1.0f);
	}
	
	/**
	 * Play the music
	 */
	public void play() {
		play(1.0f,1.0f);
	}

	/**
	 * Play the music at a given pitch and volume
	 * 
	 * @param pitch The pitch to play the music at (1.0 = default)
	 * @param volume The volume to play the music at (1.0 = default)
	 */
	public void play(float pitch, float volume) {
		if (currentMusic != null) {
			currentMusic.playing = false;
		}
		
		currentMusic = this;
		sound.playAsMusic(pitch, volume, false);
		playing = true;
	}

	/**
	 * Loop the music at a given pitch and volume
	 * 
	 * @param pitch The pitch to play the music at (1.0 = default)
	 * @param volume The volume to play the music at (1.0 = default)
	 */
	public void loop(float pitch, float volume) {
		if (currentMusic != null) {
			currentMusic.playing = false;
		}
		
		currentMusic = this;
		sound.playAsMusic(pitch, volume, true);
		playing = true;
	}
	
	/**
	 * Pause the music playback
	 */
	public void pause() {
		playing = false;
		InternalSound.pauseMusic();
	}
	
	/**
	 * Resume the music playback
	 */
	public void resume() {
		playing = true;
		InternalSound.restartMusic();
	}
	
	/**
	 * Check if the music is being played
	 * 
	 * @return True if the music is being played
	 */
	public boolean playing() {
		return (currentMusic == this) && (playing);
	}
}