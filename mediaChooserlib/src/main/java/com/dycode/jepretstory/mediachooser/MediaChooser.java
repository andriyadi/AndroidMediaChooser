/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dycode.jepretstory.mediachooser;


public class MediaChooser {

	/**
	 * Video file selected broadcast action.
	 */
	public static final String VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER = "lNc_videoSelectedAction"; 

	/**
	 *  Image file selected broadcast action.
	 */
	public static final String IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER = "lNc_imageSelectedAction"; 

	/**
	 * Set folder Name  
	 * @param folderName
	 * 			 default folder name is learnNcode.
	 */
	public static void setFolderName(String folderName) {
		if(folderName != null){
			MediaChooserConstants.folderName = folderName;
		}
	}

	/**
	 * To set visibility of camera/video button.
	 * 
	 * @param showCameraVideo 
	 *           boolean value.To set visibility of camera/video button.
	 *           default its visible.
	 */
	public static void showCameraVideoView(boolean showCameraVideo) {
		MediaChooserConstants.showCameraVideo = showCameraVideo;
	}

	/**
	 * To set file size limit for image selection.
	 * 
	 * @param size
	 *  			int file size in mb.
	 *  			default is set to 20 mb.
	 */
	public static void setImageSize(int size) {
		MediaChooserConstants.SELECTED_IMAGE_SIZE_IN_MB = size;
	}

	/**
	 * To set file size limit for video selection.
	 * 
	 * @param size
	 *  			int file size in mb.
	 *  			default is set to 20 mb.
	 */
	public static void setVideoSize(int size) {
		MediaChooserConstants.SELECTED_VIDEO_SIZE_IN_MB = size;
	}
	
	/**
	 * To set video duration limit for video selection and camera.
	 * 
	 * @param size
	 *  			int duration in second
	 *  			default is set to 15 seconds.
	 */
	public static void setVideoDurationLimit(int sec) {
		MediaChooserConstants.VIDEO_DURATION_LIMIT_IN_SECOND = sec;
	}
	
	/**
	 * Enforce video duration limit for certain seconds
	 * 
	 * @param size
	 *  			boolean enforce
	 *  			default is set to false
	 */
	public static void enforceVideoDurationLimit(boolean enforce) {
		MediaChooserConstants.ENFORCE_VIDEO_DURATION_LIMIT = enforce;
	}

	/**
	 * To set number of items that can be selected. 
	 * 
	 * @param limit
	 *   			int value.
	 *   			Default is 100.
	 */
	public static void setSelectionLimit(int limit) {
		MediaChooserConstants.MAX_MEDIA_LIMIT = limit;
	}
	
	public static void setSingleSelectionMode() {
		MediaChooserConstants.MAX_MEDIA_LIMIT = 1;
	}

	/**
	 * To set already selected file count.
	 * 
	 * @param count
	 * 				int value.
	 */
	public static void setSelectedMediaCount(int count) {
		MediaChooserConstants.SELECTED_MEDIA_COUNT = count;
	}

	/**
	 *  Get selected media file count.
	 *  
	 * @return
	 * 			count.
	 */
	public static int getSelectedMediaCount() {
		return MediaChooserConstants.SELECTED_MEDIA_COUNT ;
	}

	/**
	 * To display images only.
	 */
	public static void showOnlyImageTab() {
		MediaChooserConstants.showImage = true;
		MediaChooserConstants.showVideo = false;
	}

	/**
	 * To display video and images.
	 */
	public static void showImageVideoTab() {
		MediaChooserConstants.showImage = true;
		MediaChooserConstants.showVideo = true;
	}

	/**
	 * To display videos only.
	 */
	public static void showOnlyVideoTab() {
		MediaChooserConstants.showImage = false;
		MediaChooserConstants.showVideo = true;
	}

}
