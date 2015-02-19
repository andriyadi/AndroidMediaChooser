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


package com.dycode.jepretstory.mediachooser.async;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.dycode.jepretstory.mediachooser.MediaModel;
import com.dycode.jepretstory.mediachooser.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class ImageLoadAsync extends MediaAsync<String, String, String>{

	private ImageView mImageView;
	private Context mContext;
	private int mWidth;
	private int mPlaceholderResId = R.drawable.cover_blue;
	private MediaModel mModel;
	
	public ImageLoadAsync(Context context, ImageView imageView, int width) {
		mImageView 	= imageView;
		mContext   	= context;
		mWidth     	= width;
	}
	
	public ImageLoadAsync(Context context, ImageView imageView, int width, MediaModel model) {
		mImageView 	= imageView;
		mContext   	= context;
		mWidth     	= width;
		mModel 		= model;
	}

	private void processThumbnailImage(String imageId, MediaModel model, Context context) {
		
		// Request image related to this thumbnail
		String[] columns = { MediaStore.Images.Thumbnails.DATA }; //, MediaStore.Images.Thumbnails.IMAGE_ID};
		Cursor imagesCursor = context.getContentResolver().query(
				MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[] { imageId },
				null);
		
		if (imagesCursor != null && imagesCursor.moveToLast()) {
			int dataColumnIndex = imagesCursor.getColumnIndex(columns[0]);
			String filePath = imagesCursor.getString(dataColumnIndex);
			
//			int idColumnIndex = imagesCursor.getColumnIndex(columns[1]);
//			String id = imagesCursor.getString(idColumnIndex);
			
			model.thumbnailUrl = filePath;
			model.thumbnailId = imageId;//id;
			
			imagesCursor.close();
		} else {
			imagesCursor.close();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		String url = params[0].toString();		
		
		if (this.mModel != null) {
			if (!TextUtils.isEmpty(mModel.thumbnailUrl)) {
				return this.mModel.thumbnailUrl;
			}
			
			//get thumbnail
			processThumbnailImage(this.mModel.id, this.mModel, this.mContext);
			if (!TextUtils.isEmpty(this.mModel.thumbnailUrl)) {
				return this.mModel.thumbnailUrl;
			}
		}
		
//		File thumbnailFile = new File(model.thumbnailUrl);
//		Drawable thumbnailDrawable = Drawable.createFromPath(thumbnailFile.getAbsolutePath());
//		
//		return thumbnailDrawable;
		
		return url;
	}

	@Override
	protected void onPostExecute(String result) {

		if (this.mModel != null) {
			ExifTransformation transform = new ExifTransformation(this.mContext, this.mModel.orientation);
			
			Picasso.with(mContext).load(new File(result))
					 .resize(mWidth, mWidth, true).centerCrop().transform(transform)
					//.resize(mWidth, mWidth).centerCrop().transform(transform)
					.placeholder(mPlaceholderResId).into(mImageView);
		} else {
			Picasso.with(mContext).load(new File(result))
					 .resize(mWidth, mWidth, true).centerCrop()
					//.resize(mWidth, mWidth).centerCrop()
					.placeholder(mPlaceholderResId).into(mImageView);
		}
	}
	
	public class ExifTransformation implements Transformation {

		final Context context;
		final int exifOrientation;

		public ExifTransformation(Context context, int exifOrientation) {
			this.context = context;
			this.exifOrientation = exifOrientation;
		}
	    
		@Override
		public Bitmap transform(Bitmap source) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
				return source;
			
			if (exifOrientation != 0) {
				Matrix matrix = new Matrix();
				matrix.preRotate(exifOrientation);

				Bitmap rotated = Bitmap.createBitmap(source, 0, 0,
						source.getWidth(), source.getHeight(), matrix, true);
				if (rotated != source) {
					source.recycle();
				}
				return rotated;
			}

			return source;
		}

		@Override
		public String key() {
			return "ExifTransformation()";
		}
	}
}
