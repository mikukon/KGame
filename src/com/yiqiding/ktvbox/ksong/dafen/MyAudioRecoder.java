package com.yiqiding.ktvbox.ksong.dafen;

import android.media.AudioRecord;

public class MyAudioRecoder extends AudioRecord {

	public MyAudioRecoder(int audioSource, int sampleRateInHz,
			int channelConfig, int audioFormat, int bufferSizeInBytes)
			throws IllegalArgumentException {
		super(audioSource, sampleRateInHz, channelConfig, audioFormat,
				bufferSizeInBytes);
	}

	@Override
	protected void finalize() {
//		super.finalize();
	}
}
