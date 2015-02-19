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


package com.dycode.jepretstory.mediachooser.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.dycode.jepretstory.mediachooser.MediaModel;
import com.dycode.jepretstory.mediachooser.R;
import com.dycode.jepretstory.mediachooser.async.ImageLoadAsync;
import com.dycode.jepretstory.mediachooser.async.MediaAsync;
import com.dycode.jepretstory.mediachooser.async.VideoLoadAsync;
import com.dycode.jepretstory.mediachooser.fragment.VideoFragment;

import java.util.List;

public class GridViewAdapter extends ArrayAdapter<MediaModel> {
	public VideoFragment videoFragment;  

	private Context mContext;
	private List<MediaModel> mGalleryModelList;
	private int mWidth;
	private boolean mIsFromVideo;
	LayoutInflater viewInflater;
	

	public GridViewAdapter(Context context, int resource, List<MediaModel> categories, boolean isFromVideo) {
		super(context, resource, categories);
		mGalleryModelList = categories;
		mContext          = context;
		mIsFromVideo      = isFromVideo;
		viewInflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mGalleryModelList.size();
	}

	@Override
	public MediaModel getItem(int position) {
		return mGalleryModelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {

			mWidth = mContext.getResources().getDisplayMetrics().widthPixels;  
			
			convertView = viewInflater.inflate(R.layout.view_grid_item_media_chooser, parent, false);

			holder = new ViewHolder();
			holder.checkBoxTextView   = (CheckedTextView) convertView.findViewById(R.id.checkTextViewFromMediaChooserGridItemRowView);
			holder.imageView          = (ImageView) convertView.findViewById(R.id.imageViewFromMediaChooserGridItemRowView);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		LayoutParams imageParams = (LayoutParams) holder.imageView.getLayoutParams();
		imageParams.width  = mWidth/3;
		imageParams.height = mWidth/3;

		holder.imageView.setLayoutParams(imageParams);

		// set the status according to this Category item
		MediaModel model = mGalleryModelList.get(position);

		if (mIsFromVideo) {
			int w = mWidth/3;
			int h = mWidth/3;
			if (!TextUtils.isEmpty(model.resolution)) {
				String[] dims = model.resolution.split("x");
				if (dims.length == 2) {
					h = Math.round((Float.parseFloat(dims[1])/Float.parseFloat(dims[0]))* w);
				}
			}
			VideoLoadAsync videoLoadAsync = new VideoLoadAsync(videoFragment, holder.imageView, false, w, h);
			videoLoadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, model.url.toString());

		} else {
			//holder.imageView.setImageURI(Uri.parse(mGalleryModelList.get(position).thumbnailUrl));
			
			ImageLoadAsync loadAsync = new ImageLoadAsync(mContext, holder.imageView, mWidth/3, model);
			String url = !TextUtils.isEmpty(model.thumbnailUrl)? model.thumbnailUrl:model.url;
			//String url = model.url;
			
			try {
				loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, url);
			} catch(Exception ex) {
				
			}
		}

		holder.checkBoxTextView.setChecked(model.status);
		return convertView;
	}

	class ViewHolder {
		ImageView imageView;
		CheckedTextView checkBoxTextView;
	}

}
