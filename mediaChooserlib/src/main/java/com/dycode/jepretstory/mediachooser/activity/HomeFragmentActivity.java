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

package com.dycode.jepretstory.mediachooser.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.material.drawable.menu.MaterialMenuDrawable.IconState;
import com.balysv.material.drawable.menu.MaterialMenuIcon;
import com.dycode.jepretstory.mediachooser.MediaChooser;
import com.dycode.jepretstory.mediachooser.MediaChooserConstants;
import com.dycode.jepretstory.mediachooser.MediaChooserConstants.MediaType;
import com.dycode.jepretstory.mediachooser.MediaModel;
import com.dycode.jepretstory.mediachooser.R;
import com.dycode.jepretstory.mediachooser.async.ImageLoadAsync;
import com.dycode.jepretstory.mediachooser.async.MediaAsync;
import com.dycode.jepretstory.mediachooser.async.VideoLoadAsync;
import com.dycode.jepretstory.mediachooser.fragment.ImageFragment;
import com.dycode.jepretstory.mediachooser.fragment.VideoFragment;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.google.samples.apps.iosched.ui.widget.SlidingTabStrip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeFragmentActivity extends FragmentActivity implements ImageFragment.OnImageSelectedListener, 
VideoFragment.OnVideoSelectedListener, OnClickListener {

	protected MaterialMenuIcon materialMenu;
	
	private static Uri fileUri;

	private final Handler handler = new Handler();

	private MediaType currentMediaMode = MediaType.IMAGE;
	private Menu mMenu;
	private List<String> mTabTitles = new ArrayList<String>();
	
	private SlidingTabLayout mSlidingTabLayout;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private LinearLayout mSelectedImagesContainer;
	private TextView mSelectedImageEmptyMessage;
	
	//private int mImageSelectedCount = 0, mVideoSelectedCount = 0;
	private VideoFragment mVideoFragment;
	private ImageFragment mImageFragment;
	private ArrayList<MediaModel> mCurrentSelectedVideos = new ArrayList<MediaModel>();
	private ArrayList<MediaModel> mCurrentSelectedImages = new ArrayList<MediaModel>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_item_media_chooser);

		FrameLayout mSelectedImagesContainerFrame = (FrameLayout) findViewById(R.id.selectedPhotosContainerFrame);
		if (MediaChooserConstants.MAX_MEDIA_LIMIT == 1) {
			mSelectedImagesContainerFrame.setVisibility(View.GONE);
		}
        mSelectedImagesContainer = (LinearLayout) findViewById(R.id.selectedPhotosContainer);
        mSelectedImageEmptyMessage = (TextView) findViewById(R.id.selectedPhotosEmptyText);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        if (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false))) {

			if (getIntent().getBooleanExtra("image", false)) {

                mTabTitles.add(getResources().getString(R.string.image));
				setCurrentMode(MediaType.IMAGE);

			} else {

				mTabTitles.add(getResources().getString(R.string.video));
				setCurrentMode(MediaType.VIDEO);

			}

		} else {

			if (MediaChooserConstants.showImage) {
				mTabTitles.add(getResources().getString(R.string.image));
			}

			if (MediaChooserConstants.showVideo){
				mTabTitles.add(getResources().getString(R.string.video));
			}

		}

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout)findViewById(R.id.slidingTabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
		//mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(android.R.color.white));
		mSlidingTabLayout.setDistributeEvenly(true);
		mSlidingTabLayout.setViewPager(mViewPager);

		final android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {

        	materialMenu = new MaterialMenuIcon(this, Color.WHITE);
    		materialMenu.setState(IconState.ARROW);

    		if (mTabTitles.size() > 1) {
                mSlidingTabLayout.setVisibility(View.VISIBLE);
   		    }
    		else {
    			mSlidingTabLayout.setVisibility(View.GONE);
    		}

    		mSlidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							ActionBar ab = HomeFragmentActivity.this.getActionBar();
							ab.show();
							if (position == 0) {
								setCurrentMode(MediaType.IMAGE);
							}
							else {
								setCurrentMode(MediaType.VIDEO);
							}
						}
					});
        }

        mViewPager.setCurrentItem(0);

        //boolean isFromBucket = (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false)));
        if (getIntent().getParcelableArrayListExtra("selectedImages") != null) {
        	mCurrentSelectedImages = getIntent().getParcelableArrayListExtra("selectedImages");
        	//mImageSelectedCount = mCurrentSelectedImages.size();
			if (mCurrentSelectedImages.size() > 0) {
				onImageSelectedCount(mCurrentSelectedImages.size());
				for (MediaModel selImage : mCurrentSelectedImages) {
					this.onImageSelected(selImage);
				}
			}
        }
        if (getIntent().getParcelableArrayListExtra("selectedVideos") != null) {
        	mCurrentSelectedVideos = getIntent().getParcelableArrayListExtra("selectedVideos");
        	//mImageSelectedCount = mCurrentSelectedImages.size();
        	if (mCurrentSelectedVideos.size() > 0) {
        		onVideoSelectedCount(mCurrentSelectedVideos.size());
	        	for(MediaModel selVideo: mCurrentSelectedVideos) {
	        		this.onVideoSelected(selVideo);
	        	}
        	}
        }
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (materialMenu != null)
			materialMenu.syncState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (materialMenu != null)
			materialMenu.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			boolean isFromBucket = (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false)));
			
			String title = mTabTitles.get(position);
			if (title.equalsIgnoreCase(getString(R.string.image))) {
				mImageFragment = new ImageFragment();
				
				Bundle bundle = new Bundle();
				if (isFromBucket) {					
					bundle.putString("name", getIntent().getStringExtra("name"));
				}
				if (mCurrentSelectedImages != null) {
					bundle.putParcelableArrayList("selectedImages", mCurrentSelectedImages);
				}
				mImageFragment.setArguments(bundle);
				
				return mImageFragment;
			}
			else if (title.equalsIgnoreCase(getString(R.string.video))) {
				return getVideoFragment();
			}
			else {
				return null;
			}
		}

		@Override
		public int getCount() {
			return mTabTitles.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = mTabTitles.get(position);
			return title;
		}
	}

	private VideoFragment getVideoFragment() {
		if (mVideoFragment != null) {
			return mVideoFragment;
		}
			
		boolean isFromBucket = (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false)));
		
		mVideoFragment = new VideoFragment();		
		Bundle bundle = new Bundle();
		if (isFromBucket) {					
			bundle.putString("name", getIntent().getStringExtra("name"));
		}
		if (mCurrentSelectedVideos != null) {
			bundle.putParcelableArrayList("selectedVideos", mCurrentSelectedVideos);
		}
		mVideoFragment.setArguments(bundle);
		return mVideoFragment;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.activity_home, menu);
		
		MenuItem cameraItem = menu.findItem(R.id.menuCamera);
		if (!MediaChooserConstants.showCameraVideo){
			cameraItem.setVisible(false);
		}
		
		this.mMenu = menu;
		
		//init menu states
		if (getIntent() != null && (getIntent().getBooleanExtra("isFromBucket", false))) {
			if (getIntent().getBooleanExtra("image", false)) {
				setCurrentMode(MediaType.IMAGE);
			} else {
				setCurrentMode(MediaType.VIDEO);
			}
		} else {
			if (MediaChooserConstants.showImage) {
				setCurrentMode(MediaType.IMAGE);
			} else {
				setCurrentMode(MediaType.VIDEO);
			}
		}
				
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int itemID = item.getItemId();
		if (itemID == android.R.id.home) {
			materialMenu.animateTouch();
			finish();
		}
		else if (itemID == R.id.menuNext) {
			
			//android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
			
			if(mVideoFragment != null || mImageFragment != null){

				if(mVideoFragment != null){
					if(mVideoFragment.getSelectedVideos() != null && mVideoFragment.getSelectedVideos() .size() > 0){
						Intent videoIntent = new Intent();
						videoIntent.setAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER );
						//videoIntent.putStringArrayListExtra("list", mVideoFragment.getSelectedVideoList());
						videoIntent.putParcelableArrayListExtra("selectedVideos", mVideoFragment.getSelectedVideos());
						setResult(RESULT_OK, videoIntent);
						sendBroadcast(videoIntent);
					}
				}

				if(mImageFragment != null){
					if(mImageFragment.getSelectedImages() != null && mImageFragment.getSelectedImages().size() > 0){
						Intent imageIntent = new Intent();
						imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
						//imageIntent.putStringArrayListExtra("list", mImageFragment.getSelectedImageList());
						imageIntent.putParcelableArrayListExtra("selectedImages", mImageFragment.getSelectedImages());
						setResult(RESULT_OK, imageIntent);
						sendBroadcast(imageIntent);
					}
				}

				finish();
				
			}else{
				Toast.makeText(HomeFragmentActivity.this, getString(R.string.plaese_select_file), Toast.LENGTH_SHORT).show();
			}
		}
		else if (itemID == R.id.menuCamera) {
			
			if(currentMediaMode == MediaType.VIDEO){
				
				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				fileUri = getOutputMediaFileUri(MediaType.VIDEO); // create a file to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
				Long limit = Long.valueOf((MediaChooserConstants.SELECTED_VIDEO_SIZE_IN_MB * 1024 * 1024));
				intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limit);
				intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MediaChooserConstants.VIDEO_DURATION_LIMIT_IN_SECOND);
				
				// start the image capture Intent
				startActivityForResult(intent, MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

			}else{
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				fileUri = getOutputMediaFileUri(MediaType.IMAGE); // create a file to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name				
				
				// start the image capture Intent
				startActivityForResult(intent, MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
			
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setCurrentMode(MediaType mode) {
		currentMediaMode = mode;
		if (currentMediaMode == MediaType.IMAGE) {
			if (mMenu != null) {
				MenuItem item = mMenu.findItem(R.id.menuCamera);
				item.setIcon(R.drawable.selector_camera_button);
				item.setTitle(getString(R.string.image));		
			}
			
			getActionBar().setTitle(getString(R.string.image));
		}
		else {
			if (mMenu != null) {
				MenuItem item = mMenu.findItem(R.id.menuCamera);
				item.setIcon(R.drawable.selector_video_button);
				item.setTitle(getString(R.string.video));		
			}				
			getActionBar().setTitle(getString(R.string.video));
		}
	}
	
	private void setTabText(MediaType mode, String text) {
		
		TextView textView = null;
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
		
			SlidingTabStrip v = (SlidingTabStrip)mSlidingTabLayout.getChildAt(0);
			TextView currentTextView = (TextView) v.getChildAt(i);//currentTab.findViewById(android.R.id.text1);
			String currentText = currentTextView.getText().toString();
			
			if (mode == MediaType.IMAGE && currentText.startsWith(getResources().getString(R.string.image).trim())) {
				textView = currentTextView;
				break;
			}
			if (mode == MediaType.VIDEO && currentText.startsWith(getResources().getString(R.string.video).trim())) {
				textView = currentTextView;
				break;
			}
		}

		if (textView != null) {
			textView.setText(text);
		}
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(MediaType type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(MediaType type){

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MediaChooserConstants.folderName);
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MediaType.IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if(type == MediaType.VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK ) {

				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
				final AlertDialog alertDialog = MediaChooserConstants.getDialog(HomeFragmentActivity.this).create();
				alertDialog.show();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//Do something after 5000ms
						String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
						//android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
						if(mVideoFragment == null){
							VideoFragment newVideoFragment = new VideoFragment();
							newVideoFragment.addItem(fileUriString);

						} else {
							mVideoFragment.addItem(fileUriString);
						}
						alertDialog.cancel();
					}
				}, 5000);


			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
			}
		}else if (requestCode == MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK ) {

				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));

				final AlertDialog alertDialog = MediaChooserConstants.getDialog(HomeFragmentActivity.this).create();
				alertDialog.show();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//Do something after 5000ms
						String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();
						if (mImageFragment == null){
							ImageFragment newImageFragment = new ImageFragment();
							newImageFragment.addItem(fileUriString);

						} else {
							mImageFragment.addItem(fileUriString);
						}
						alertDialog.cancel();
					}
				}, 5000);
			} 
		}
	}

	@Override
	public void onImageSelectedCount(int count) {
		
		//mImageSelectedCount = count;
		
		String text = count > 0? (getResources().getString(R.string.images_tab) + "  (" + count + ")"): getResources().getString(R.string.image);
		setTabText(MediaType.IMAGE, text);
	}

	private void onMediaSelected(MediaModel media) {
		
		View rootView = LayoutInflater.from(HomeFragmentActivity.this).inflate(R.layout.list_item_selected_thumbnail, null);
		ImageView thumbnail = (ImageView) rootView.findViewById(R.id.selected_photo);
		rootView.setTag(media.url);
		rootView.setOnClickListener(this);
		
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
		thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));
		
		/*
		if (media.mediaMode == MediaMode.IMAGE) {
			Picasso.with(this).load(new File(media.url)).resize(px, px)
					.centerCrop().placeholder(R.drawable.cover_blue)
					.into(thumbnail);
		}
		else {
			
		}
		*/
		
		if(media.mediaMode == MediaType.VIDEO){
			new VideoLoadAsync(getVideoFragment(), thumbnail, false, px, px).executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, media.url);

		}else{
			ImageLoadAsync loadAsync = new ImageLoadAsync(this, thumbnail, px);
			loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, media.url);
		}
		
		mSelectedImagesContainer.addView(rootView, 0);
		
		//if (mImageSelectedCount >= 1 || mVideoSelectedCount >= 1) {
		if (mSelectedImagesContainer.getChildCount() > 0) {
			mSelectedImagesContainer.setVisibility(View.VISIBLE);
			mSelectedImageEmptyMessage.setVisibility(View.GONE);
		}		
	}

	private void onMediaUnselected(MediaModel image) {
		
		for (int i = 0; i < mSelectedImagesContainer.getChildCount(); i++) {
			View childView = mSelectedImagesContainer.getChildAt(i);
			if (childView.getTag().equals(image.url)) {
				mSelectedImagesContainer.removeViewAt(i);
				break;
			}
		}
		//if (mImageSelectedCount == 0 && mVideoSelectedCount == 0) {
		if (mSelectedImagesContainer.getChildCount() == 0) {
			mSelectedImagesContainer.setVisibility(View.GONE);
			mSelectedImageEmptyMessage.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onImageSelected(MediaModel image) {		
		onMediaSelected(image);		
	}
	
	@Override
	public void onImageUnselected(MediaModel image) {		
		onMediaUnselected(image);
	}

	@Override
	public void onVideoSelectedCount(int count){
		
		//mVideoSelectedCount = count;
		
		String text = count > 0? (getResources().getString(R.string.videos_tab) + "  (" + count + ")"): getResources().getString(R.string.video);
		setTabText(MediaType.VIDEO, text);
	}
	
	@Override
	public void onVideoSelected(MediaModel video) {		
		onMediaSelected(video);		
	}
	
	@Override
	public void onVideoUnselected(MediaModel video) {		
		onMediaUnselected(video);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v instanceof FrameLayout) {
			//deselect
			String url = (String)v.getTag();
			MediaModel model = null;
			if (mCurrentSelectedImages != null) {
				for(MediaModel m: mCurrentSelectedImages) {
					if (m.url.equals(url)) {
						model = m;
						break;
					}
				}
			}
			if (model == null && mCurrentSelectedVideos != null) {
				for(MediaModel m: mCurrentSelectedVideos) {
					if (m.url.equals(url)) {
						model = m;
						break;
					}
				}
			}
			if (model != null) {
				if (model.isVideo()) {
					mCurrentSelectedVideos.remove(model);
					mVideoFragment.unselect(model);
					onVideoSelectedCount(mCurrentSelectedVideos.size());
					onVideoUnselected(model);
					
				} else {
					mCurrentSelectedImages.remove(model);
					mImageFragment.unselect(model);
					onImageSelectedCount(mCurrentSelectedImages.size());
					onImageUnselected(model);
				}				
			}
		}
	}
}
