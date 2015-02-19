/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * Heavily modified by Andri Yadi (andri.yadi@gmail.com)
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

import com.dycode.jepretstory.mediachooser.MediaChooserConstants.MediaType;

import android.os.Parcelable;
import android.os.Parcel;

public class MediaModel implements Parcelable {

	public String url = null;
	public boolean status = false;
	public MediaType mediaMode;
	public String id, bucketId, thumbnailId;
	public String thumbnailUrl = null;
	public int orientation;
	public String resolution; //WxH
	public int videoDuration = 0;
	
	public MediaModel(String url, boolean status) {
		this.url = url;
		this.status = status;
	}
	
	public MediaModel(String url, boolean status, MediaType mode) {
		this.url = url;
		this.status = status;
		this.mediaMode = mode;
	}
	
	public MediaModel(String bucketId, String id, String url, boolean status, MediaType mode) {
		this(url, status, mode);
		this.id = id;
		this.bucketId = bucketId;
	}
	
	public MediaModel(String bucketId, String id, String url, String thumbnailId, String thumbnailUrl, boolean status, MediaType mode) {
		this(bucketId, id, url, status, mode);
		this.thumbnailId = thumbnailId;
		this.thumbnailUrl = thumbnailUrl;
	}

	public int getVideoDurationInSeconds() {
		return (videoDuration/1000);
	}
	
	public boolean isVideo() {
		return mediaMode == MediaType.VIDEO;
	}
	
	public static Parcelable.Creator<MediaModel> CREATOR = new Parcelable.Creator<MediaModel>(){
		public MediaModel createFromParcel(Parcel source) {
			return new MediaModel(source);
		}
		public MediaModel[] newArray(int size) {
			return new MediaModel[size];
		}
	};
	
	private MediaModel(Parcel in) {
		this.url = in.readString();
		this.status = in.readByte() != 0;
		int tmpMediaMode = in.readInt();
		this.mediaMode = tmpMediaMode == -1 ? null : MediaType.values()[tmpMediaMode];
		this.id = in.readString();
		this.bucketId = in.readString();
		this.thumbnailId = in.readString();
		this.thumbnailUrl = in.readString();
		this.orientation = in.readInt();
		this.resolution = in.readString();
		this.videoDuration = in.readInt();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.url);
		dest.writeByte(status ? (byte) 1 : (byte) 0);
		dest.writeInt(this.mediaMode == null ? -1 : this.mediaMode.ordinal());
		dest.writeString(this.id);
		dest.writeString(this.bucketId);
		dest.writeString(this.thumbnailId);
		dest.writeString(this.thumbnailUrl);
		dest.writeInt(this.orientation);
		dest.writeString(this.resolution);
		dest.writeInt(this.videoDuration);
	}
	
	@Override
	public boolean equals(Object o) {
		
		MediaModel source = (MediaModel)o;
		if (source.url == null || this.url == null) {
			return false;
		}
		//boolean res = ((source.url != null && source.url.equals(this.url)) || (source.thumbnailUrl != null && source.thumbnailUrl.equals(this.thumbnailUrl))) && source.mediaMode == this.mediaMode;
		boolean res = source.url.equals(this.url) && source.mediaMode == this.mediaMode;
		
		return res;
	};
}
