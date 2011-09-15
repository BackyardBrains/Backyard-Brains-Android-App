package com.backyardbrains.audio;

import java.nio.ByteBuffer;

/**
 * A simple interface to attach to services which allows a callback into
 * android's built-in classes from an unrelated thread
 * 
 * @author Nathan Dotz <nate@backyardbrains.com>
 * 
 */
public interface ReceivesAudio {
	/**
	 * Called by a thread to pass audio {@link ByteBuffer} into a service. The
	 * service should then do as it sees fit with the data.
	 * 
	 * @param audioInfo
	 */
	public void receiveAudio(byte[] audioInfo);
}