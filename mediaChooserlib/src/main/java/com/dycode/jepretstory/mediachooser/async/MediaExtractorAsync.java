package com.dycode.jepretstory.mediachooser.async;

import java.io.IOException;

import android.media.MediaExtractor;
import android.media.MediaFormat;

public class MediaExtractorAsync extends MediaAsync<String, String, Object> {

	@Override
	protected Object doInBackground(String... params) {
		// TODO Auto-generated method stub
		/*String filePath = params[0];
		
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(filePath);
			
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					extractor.selectTrack(i);
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return null;
	}

}
