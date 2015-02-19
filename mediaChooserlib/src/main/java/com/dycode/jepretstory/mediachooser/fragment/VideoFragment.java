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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

public class VideoFragment extends Fragment implements OnScrollListener, ObservableScrollViewCallbacks {

	private static final String[] QUERY_COLUMNS = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.RESOLUTION, MediaStore.Video.Media.BUCKET_ID};
	private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;

	private GridViewAdapter mVideoAdapter;
	private ObservableGridView mVideoGridView;
	private Cursor mCursor;
	private int mDataColumnIndex;
	private ArrayList<MediaModel> mSelectedModels = new ArrayList<MediaModel>();
	private ArrayList<MediaModel> mGalleryModelList;
	private View mView;
	private OnVideoSelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnVideoSelectedListener {
		public void onVideoSelectedCount(int count);
		public void onVideoSelected(MediaModel image);
		public void onVideoUnselected(MediaModel image);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnVideoSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnVideoSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public VideoFragment(){
		setRetainInstance(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if(mView == null){
			mView = inflater.inflate(R.layout.view_grid_layout_media_chooser, container, false);

			mVideoGridView = (ObservableGridView)mView.findViewById(R.id.gridViewFromMediaChooser);
			mVideoGridView.setScrollViewCallbacks(this);
			
			if (getArguments() != null) {
				if (getArguments().getStringArrayList("selectedVideos") != null) {
					//mSelectedItems = getArguments().getStringArrayList("selectedVideos");
					mSelectedModels = getArguments().getParcelableArrayList("selectedVideos");
				}
				if (getArguments().getString("name") != null) {
					initVideos(getArguments().getString("name"));
				}
				else {
					initVideos();
				}
			}else{
				initVideos();
			}

		}else{
			((ViewGroup) mView.getParent()).removeView(mView);
			if(mVideoAdapter == null || mVideoAdapter.getCount() == 0){
				Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
			}
		}

		return mView;
	};


	private void initVideos(String bucketName) {

		try {
			final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
			String searchParams = null;
			searchParams = "bucket_display_name = \"" + bucketName + "\"";

			//final String[] columns = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID};
			mCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, QUERY_COLUMNS, searchParams, null, orderBy + " DESC");
			setAdapter();
			mCursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initVideos() {

		try {
			final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
			//Here we set up a string array of the thumbnail ID column we want to get back

			//String[] columns = {MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID};
			mCursor =  getActivity().getContentResolver().query(MEDIA_EXTERNAL_CONTENT_URI, QUERY_COLUMNS, null,null, orderBy + " DESC");
			setAdapter();
			mCursor.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setAdapter() {
		int count = mCursor.getCount();

		if(count > 0){
			mDataColumnIndex = mCursor.getColumnIndex(MEDIA_DATA);
			int mIdColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media._ID);
			int mBucketColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
			int resColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);
			int durColumnIndex = mCursor.getColumnIndex(MediaStore.Video.Media.DURATION);

			//move position to first element
			mCursor.moveToFirst();

			mGalleryModelList = new ArrayList<MediaModel>();
			for(int i= 0; i < count; i++) {
				mCursor.moveToPosition(i);
				String url = mCursor.getString(mDataColumnIndex);
				String id = mCursor.getString(mIdColumnIndex);
				String bucketId = mCursor.getString(mBucketColumnIndex);
				
				//mGalleryModelList.add(new MediaModel(url, false, MediaMode.VIDEO));
				
//				boolean selected = false;
//				if (mSelectedItems != null && mSelectedItems.size() > 0) {
//					for(String currVidUrl: mSelectedItems) {
//						if (currVidUrl.equalsIgnoreCase(url)) {
//							selected = true;
//							break;
//						}
//					}
//				}
				
				MediaModel galleryModel   = new MediaModel(bucketId, id, url, false, MediaType.VIDEO);	
				if (mSelectedModels != null && mSelectedModels.size() > 0) {
					galleryModel.status = mSelectedModels.contains(galleryModel);
				}
				
				galleryModel.resolution = mCursor.getString(resColumnIndex);
				galleryModel.videoDuration = mCursor.getInt(durColumnIndex);
				
				mGalleryModelList.add(galleryModel);				
			}

			mVideoAdapter =  new GridViewAdapter(getActivity(), 0, mGalleryModelList, true);
			mVideoAdapter.videoFragment = this;
			mVideoGridView.setAdapter(mVideoAdapter);
			mVideoGridView.setOnScrollListener(this);
			
		}else{
			Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();

		}


		mVideoGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
				MediaModel galleryModel = (MediaModel) adapter.getItem(position);
				File file = new File(galleryModel.url);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "video/*");
				startActivity(intent);
				return false;
			}
		});

		mVideoGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// update the mStatus of each category in the adapter
				GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
				MediaModel galleryModel = (MediaModel) adapter.getItem(position);

				if(! galleryModel.status){
					long size = MediaChooserConstants.ChekcMediaFileSize(new File(galleryModel.url.toString()), true);
					if(size != 0){
						Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.file_size_exeeded) + "  " + MediaChooserConstants.SELECTED_VIDEO_SIZE_IN_MB + " " +  getActivity().getResources().getString(R.string.mb), Toast.LENGTH_SHORT).show();
						return;
					}

					if (MediaChooserConstants.ENFORCE_VIDEO_DURATION_LIMIT && galleryModel.getVideoDurationInSeconds() > MediaChooserConstants.VIDEO_DURATION_LIMIT_IN_SECOND) {
						String fmt = getActivity().getResources().getString(R.string.video_duration_limit_exeeded);
						String msg = String.format(fmt, MediaChooserConstants.VIDEO_DURATION_LIMIT_IN_SECOND);
						Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
					mSelectedModels.add(galleryModel);
					
					MediaChooserConstants.SELECTED_MEDIA_COUNT ++;

				} else {
					mSelectedModels.remove(galleryModel);
					
					MediaChooserConstants.SELECTED_MEDIA_COUNT --;
				}

				if (mCallback != null) {
					
					mCallback.onVideoSelectedCount(mSelectedModels.size());
					
					if (galleryModel.status) {
						mCallback.onVideoSelected(galleryModel);
					}
					else {
						mCallback.onVideoUnselected(galleryModel);
					}
					
					Intent intent = new Intent();
					intent.putParcelableArrayListExtra("selectedVideos", mSelectedModels);
					getActivity().setResult(Activity.RESULT_OK, intent);
				}

			}
		});

	}

	public void addItem(String item) {
		if(mVideoAdapter != null){
			MediaModel model = new MediaModel(item, false, MediaType.VIDEO);
			mGalleryModelList.add(0, model);
			mVideoAdapter.notifyDataSetChanged();
		}else{
			initVideos();
		}
	}

	public void unselect(MediaModel item) {
		if(mVideoAdapter != null){
			item.status = false;
			mSelectedModels.remove(item);
			mVideoAdapter.notifyDataSetChanged();
		}
	}
	
	public GridViewAdapter getAdapter() {
		if (mVideoAdapter != null) {
			return mVideoAdapter;
		}
		return null;
	}

//	public ArrayList<String> getSelectedVideoList() {
//		return mSelectedItems;
//	}

	
	public ArrayList<MediaModel> getSelectedVideos() {
		return mSelectedModels;
	}
	
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//		if (view.getId() == android.R.id.list) {
		if (view == mVideoGridView) {
			// Set scrolling to true only if the user has flinged the
			// ListView away, hence we skip downloading a series
			// of unnecessary bitmaps that the user probably
			// just want to skip anyways. If we scroll slowly it
			// will still download bitmaps - that means
			// that the application won't wait for the user
			// to lift its finger off the screen in order to
			// download.
			if (scrollState == SCROLL_STATE_FLING) {
				//chk
			} else {
				mVideoAdapter.notifyDataSetChanged();
			}
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}
	
	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDownMotionEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		// TODO Auto-generated method stub
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

