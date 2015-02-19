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


package com.dycode.jepretstory.mediachooser.fragment;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.dycode.jepretstory.mediachooser.MediaChooserConstants;
import com.dycode.jepretstory.mediachooser.MediaModel;
import com.dycode.jepretstory.mediachooser.MediaChooserConstants.MediaType;
import com.dycode.jepretstory.mediachooser.adapter.GridViewAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableGridView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.dycode.jepretstory.mediachooser.R;


public class ImageFragment extends Fragment implements ObservableScrollViewCallbacks {

    private ArrayList<MediaModel> mSelectedModels = new ArrayList<MediaModel>();
	private ArrayList<MediaModel> mGalleryModelList;
	private ObservableGridView mImageGridView;
	private View mView;
	private OnImageSelectedListener mCallback;
	private GridViewAdapter mImageAdapter;
	private Cursor mImageCursor;

	// Container Activity must implement this interface
	public interface OnImageSelectedListener {
		public void onImageSelectedCount(int count);
		public void onImageSelected(MediaModel image);
		public void onImageUnselected(MediaModel image);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnImageSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnImageSelectedListener");
		}
	}

	public ImageFragment(){
		setRetainInstance(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if(mView == null){
			mView = inflater.inflate(R.layout.view_grid_layout_media_chooser, container, false);

			mImageGridView = (ObservableGridView) mView.findViewById(R.id.gridViewFromMediaChooser);
			mImageGridView.setScrollViewCallbacks(this);

			if (getArguments() != null) {
				if (getArguments().getStringArrayList("selectedImages") != null) {
					mSelectedModels = getArguments().getParcelableArrayList("selectedImages");
				}
				if (getArguments().getString("name") != null) {
					initPhoneImages(getArguments().getString("name"));
				}
				else {
					initPhoneImages();
				}
				
			}else{
				initPhoneImages();
			}

		}else{
			((ViewGroup) mView.getParent()).removeView(mView);
			if(mImageAdapter == null || mImageAdapter.getCount() == 0){
				Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
			}
		}

		return mView;
	}

	private void initPhoneImages(String bucketName){
		try {
			final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
			String searchParams = null;
			String bucket = bucketName;
			searchParams = "bucket_display_name = \"" + bucket + "\"";

			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.ImageColumns.ORIENTATION};
			mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy + " DESC");

			setAdapter(mImageCursor);
			mImageCursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initPhoneImages() {
		try {
			final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.ImageColumns.ORIENTATION};
			//final String[] columns = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID};
			
			mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
			//mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, columns, null, null, null);

			setAdapter(mImageCursor);			
			mImageCursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	private void processFullImageFromThumbnailId(String thumbnailId, MediaModel model, Context context) {
		
		// Request image related to this thumbnail
		String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID};
		Cursor imagesCursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Media._ID + "=?", new String[] { thumbnailId },
				null);
		
		if (imagesCursor != null && imagesCursor.moveToFirst()) {
			int dataColumnIndex = imagesCursor.getColumnIndex(columns[0]);
			String filePath = imagesCursor.getString(dataColumnIndex);
			
			int idColumnIndex = imagesCursor.getColumnIndex(columns[1]);
			String id = imagesCursor.getString(idColumnIndex);
			
			int bucketIdColumnIndex = imagesCursor.getColumnIndex(columns[1]);
			String bucketId = imagesCursor.getString(bucketIdColumnIndex);
			
			model.url = filePath;
			model.id = id;
			model.bucketId = bucketId;
			
			imagesCursor.close();
		} else {
			imagesCursor.close();
		}
	}*/
	
	/*
	private void processThumbnailImage(String imagelId, MediaModel model, Context context) {
		
		// Request image related to this thumbnail
		String[] columns = { MediaStore.Images.Thumbnails.DATA };//, MediaStore.Images.Thumbnails.IMAGE_ID};
		Cursor imagesCursor = context.getContentResolver().query(
				MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[] { imagelId },
				null);
		
		if (imagesCursor != null && imagesCursor.moveToLast()) {
			int dataColumnIndex = imagesCursor.getColumnIndex(columns[0]);
			String filePath = imagesCursor.getString(dataColumnIndex);
			
//			int idColumnIndex = imagesCursor.getColumnIndex(columns[1]);
//			String id = imagesCursor.getString(idColumnIndex);
			
			model.thumbnailUrl = filePath;
			model.thumbnailId = imagelId;//id;
			
			imagesCursor.close();
		} else {
			imagesCursor.close();
		}
	}*/

	private void setAdapter(Cursor imagecursor) {

		if(imagecursor.getCount() > 0){

			mGalleryModelList = new ArrayList<MediaModel>();

			for (int i = 0; i < imagecursor.getCount(); i++) {
				imagecursor.moveToPosition(i);
				int dataColumnIndex       	= imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
				//int thumbnailColumnIndex    = imagecursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
				int idColumnIndex 			= imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
				//int idColumnIndex 			= imagecursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);
				int bucketColumnIndex 		= imagecursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
				int orientColumnIndex 		= imagecursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
				
				String imgUrl = imagecursor.getString(dataColumnIndex).toString();
//				boolean selected = false;
//				if (mSelectedModels != null && mSelectedModels.size() > 0) {
//					for(String currImgUrl: mSelectedItems) {
//						if (currImgUrl.equalsIgnoreCase(imgUrl)) {
//							selected = true;
//							break;
//						}
//					}
//				}
				
				String id = imagecursor.getString(idColumnIndex);
				String thumbId = "";//imagecursor.getString(idColumnIndex);
				String bucketId = imagecursor.getString(bucketColumnIndex);
				String thumbUrl = "";//imagecursor.getString(thumbnailColumnIndex).toString();
				int orientation = imagecursor.getInt(orientColumnIndex);
				
				MediaModel galleryModel = new MediaModel(bucketId, id, imgUrl, thumbId, thumbUrl, false, MediaType.IMAGE);				
				if (mSelectedModels != null && mSelectedModels.size() > 0) {
					galleryModel.status = mSelectedModels.contains(galleryModel);
				}
				galleryModel.orientation = orientation;
				
				//processThumbnailImage(id, galleryModel, getActivity());
				
				mGalleryModelList.add(galleryModel);
			}


			mImageAdapter = new GridViewAdapter(getActivity(), 0, mGalleryModelList, false);
			mImageGridView.setAdapter(mImageAdapter);
		}else{
			Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
		}
		
		mImageGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
				MediaModel galleryModel = (MediaModel) adapter.getItem(position);
				File file = new File(galleryModel.url);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/*");
				startActivity(intent);
				return true;
			}
		});

		mImageGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				// update the mStatus of each category in the adapter
				GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
				MediaModel galleryModel = (MediaModel) adapter.getItem(position);


				if(! galleryModel.status){
					long size = MediaChooserConstants.ChekcMediaFileSize(new File(galleryModel.url.toString()), false);
					if(size != 0){
						Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.file_size_exeeded) + "  " + MediaChooserConstants.SELECTED_IMAGE_SIZE_IN_MB + " " +  getActivity().getResources().getString(R.string.mb), Toast.LENGTH_SHORT).show();
						return;
					}

					if (MediaChooserConstants.MAX_MEDIA_LIMIT == 1) {
						//remove all first
						if (mSelectedModels.size() >= 1) {
							
							MediaModel selModel = (MediaModel)mSelectedModels.get(0);
							selModel.status = false;
							
							mSelectedModels.clear();
							MediaChooserConstants.SELECTED_MEDIA_COUNT --;
						}
					}
					
					if((MediaChooserConstants.MAX_MEDIA_LIMIT == MediaChooserConstants.SELECTED_MEDIA_COUNT)){
						if (MediaChooserConstants.SELECTED_MEDIA_COUNT < 2) {
							Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " +  getActivity().getResources().getString(R.string.file), Toast.LENGTH_SHORT).show();
							return;
						} else {
							Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " +  getActivity().getResources().getString(R.string.files), Toast.LENGTH_SHORT).show();
							return;
						}

					}
				}

				// inverse the status
				galleryModel.status = ! galleryModel.status;

				adapter.notifyDataSetChanged();

				if (galleryModel.status) {
					//mSelectedItems.add(galleryModel.url.toString());
					mSelectedModels.add(galleryModel);
					MediaChooserConstants.SELECTED_MEDIA_COUNT ++;

				}else{
					//mSelectedItems.remove(galleryModel.url.toString().trim());
					mSelectedModels.remove(galleryModel);
					MediaChooserConstants.SELECTED_MEDIA_COUNT --;
				}

				if (mCallback != null) {
					
					mCallback.onImageSelectedCount(mSelectedModels.size());
					
					if (galleryModel.status) {
						mCallback.onImageSelected(galleryModel);
					}
					else {
						mCallback.onImageUnselected(galleryModel);
					}
					
					Intent intent = new Intent();
					//intent.putStringArrayListExtra("list", mSelectedItems);
					intent.putParcelableArrayListExtra("selectedImages", mSelectedModels);
					getActivity().setResult(Activity.RESULT_OK, intent);
				}

			}
		});
	}

//	public ArrayList<String> getSelectedImageList() {
//		return mSelectedItems;
//	}
	
	public ArrayList<MediaModel> getSelectedImages() {
		return mSelectedModels;
	}

	public void addItem(String item) {
		if(mImageAdapter != null){
			MediaModel model = new MediaModel(item, false, MediaType.IMAGE);
			mGalleryModelList.add(0, model);
			mImageAdapter.notifyDataSetChanged();
		}else{
			initPhoneImages();
		}
	}
	
	public void unselect(MediaModel item) {
		if(mImageAdapter != null){
			item.status = false;
			mSelectedModels.remove(item);
			mImageAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll,
			boolean dragging) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDownMotionEvent() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		ActionBar ab = getActivity().getActionBar();
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
	}
}
