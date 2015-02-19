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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import com.dycode.jepretstory.mediachooser.fragment.BucketImageFragment;
import com.dycode.jepretstory.mediachooser.fragment.BucketVideoFragment;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.google.samples.apps.iosched.ui.widget.SlidingTabStrip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BucketHomeFragmentActivity extends FragmentActivity implements OnClickListener {

	protected MaterialMenuIcon materialMenu;
	
	private static Uri fileUri;

	private ArrayList<MediaModel> mSelectedVideos = new ArrayList<MediaModel>();
	private ArrayList<MediaModel> mSelectedImages = new ArrayList<MediaModel>();
	
	private final Handler handler = new Handler();

	private MediaType currentMediaMode = MediaType.IMAGE;
	private Menu mMenu;

	private List<String> mTabTitles = new ArrayList<String>();
	
	private SlidingTabLayout mSlidingTabLayout;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private LinearLayout mSelectedImagesContainer;
	private TextView mSelectedImageEmptyMessage;
	
	private BucketVideoFragment mVideoFragment;
	private BucketImageFragment mImageFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home_media_chooser);

		FrameLayout mSelectedImagesContainerFrame = (FrameLayout) findViewById(R.id.selectedPhotosContainerFrame);
		if (MediaChooserConstants.MAX_MEDIA_LIMIT == 1) {
			mSelectedImagesContainerFrame.setVisibility(View.GONE);
		}
		mSelectedImagesContainer = (LinearLayout) findViewById(R.id.selectedPhotosContainer);
		mSelectedImageEmptyMessage = (TextView) findViewById(R.id.selectedPhotosEmptyText);
        mViewPager = (ViewPager) findViewById(R.id.pager);
		
        if (MediaChooserConstants.showImage) {
			mTabTitles.add(getResources().getString(R.string.image));
		}

		if (MediaChooserConstants.showVideo) {
			mTabTitles.add(getResources().getString(R.string.video));
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
			
			materialMenu = new MaterialMenuIcon(this, Color.WHITE);
    		materialMenu.setState(IconState.ARROW);
    		
    		if (mTabTitles.size() > 1) {}
    		else {
    			mSlidingTabLayout.setVisibility(View.GONE);
    		}
    		
    		mSlidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							ActionBar ab = BucketHomeFragmentActivity.this.getActionBar();
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
		
		if (getIntent().getParcelableArrayListExtra("selectedImages") != null) {
        	mSelectedImages = getIntent().getParcelableArrayListExtra("selectedImages");
        	for(MediaModel selImage: mSelectedImages) {
        		this.onMediaSelected(selImage);
        	}
        }
        if (getIntent().getParcelableArrayListExtra("selectedVideos") != null) {
        	mSelectedVideos = getIntent().getParcelableArrayListExtra("selectedVideos");
        	for(MediaModel selVideo: mSelectedVideos) {
        		this.onMediaSelected(selVideo);
        	}
        }
	}
	
	private BucketVideoFragment getVideoFragment() {
		if (mVideoFragment != null) {
			return mVideoFragment;
		}
			
		mVideoFragment = new BucketVideoFragment();		
		if (mSelectedVideos != null) {
			mVideoFragment.setCurrentSelectedVideos(mSelectedVideos);
		}
		return mVideoFragment;
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			String title = mTabTitles.get(position);
			if (title.equalsIgnoreCase(getString(R.string.image))) {
				mImageFragment = new BucketImageFragment();				
				if (mSelectedImages != null) {
					mImageFragment.setCurrentSelectedImages(mSelectedImages);
				}
				
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

	private void setTabText(MediaType mode, String text) {
		
		if (mTabTitles.size() == 1) {
			getActionBar().setTitle(text);
			return;
		}
		
		TextView textView = null;
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
		
			SlidingTabStrip v = (SlidingTabStrip)mSlidingTabLayout.getChildAt(0);
			Log.d("Media Chooser", v.getClass().getSimpleName());
			//SlidingTabStrip currentTab = (SlidingTabStrip)v.getChildAt(i);
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

	private void setCurrentMode(MediaType mode) {
		currentMediaMode = mode;
		if (currentMediaMode == MediaType.IMAGE) {
			
			if (mMenu != null) {
				MenuItem item = mMenu.findItem(R.id.menuCamera);
				item.setTitle(getString(R.string.image));
				item.setIcon(R.drawable.selector_camera_button);
			}
			getActionBar().setTitle(getString(R.string.image));
			
		} else {
			
			if (mMenu != null) {
				MenuItem item = mMenu.findItem(R.id.menuCamera);
				item.setTitle(getString(R.string.video));
				item.setIcon(R.drawable.selector_video_button);
			}
			getActionBar().setTitle(getString(R.string.video));
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_home, menu);
		
		MenuItem cameraItem = menu.findItem(R.id.menuCamera);
		if (!MediaChooserConstants.showCameraVideo){
			cameraItem.setVisible(false);
		}
		
		this.mMenu = menu;

		setCurrentMode(MediaType.IMAGE);

		// init
		if (MediaChooserConstants.showImage) {
			setCurrentMode(MediaType.IMAGE);
		} else {
			setCurrentMode(MediaType.VIDEO);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemID = item.getItemId();
		if (itemID == android.R.id.home) {
			materialMenu.animateTouch();
			finish();
		} else if (itemID == R.id.menuNext) {

			if (mSelectedImages.size() == 0 && mSelectedImages.size() == 0) {
				Toast.makeText(BucketHomeFragmentActivity.this,
						getString(R.string.plaese_select_file),
						Toast.LENGTH_SHORT).show();

			} else {

				if (mSelectedVideos.size() > 0) {
					Intent videoIntent = new Intent();
					videoIntent.setAction(MediaChooser.VIDEO_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
					//videoIntent.putStringArrayListExtra("list", mSelectedVideo);
					videoIntent.putParcelableArrayListExtra("selectedVideos", mSelectedVideos);
					setResult(RESULT_OK, videoIntent);
					sendBroadcast(videoIntent);
				}

				if (mSelectedImages.size() > 0) {
					Intent imageIntent = new Intent();
					imageIntent.setAction(MediaChooser.IMAGE_SELECTED_ACTION_FROM_MEDIA_CHOOSER);
					//imageIntent.putStringArrayListExtra("list", mSelectedImage);
					imageIntent.putParcelableArrayListExtra("selectedImages", mSelectedImages);
					setResult(RESULT_OK, imageIntent);
					sendBroadcast(imageIntent);
				}
				finish();
			}
		} else if (itemID == R.id.menuCamera) {

			if (currentMediaMode == MediaType.VIDEO) {

				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				
				// create a file to save the image
				fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_VIDEO); 
				
				//fileUri = getVideoInMediaStore(fileUri);
				
				// set the image file name
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); 
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
				Long limit = Long.valueOf((MediaChooserConstants.SELECTED_VIDEO_SIZE_IN_MB * 1024 * 1024));
				intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limit);
				intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MediaChooserConstants.VIDEO_DURATION_LIMIT_IN_SECOND);

				// start the image capture Intent
				startActivityForResult(intent, MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

			} else {

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				// create a file to save the image
				fileUri = getOutputMediaFileUri(MediaChooserConstants.MEDIA_TYPE_IMAGE); 
						
				// set the image file name
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); 

				// start the image capture Intent
				startActivityForResult(intent, MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}

		}

		return super.onOptionsItemSelected(item);
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MediaChooserConstants.folderName);
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MediaChooserConstants.MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if (type == MediaChooserConstants.MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {

			if (requestCode == MediaChooserConstants.BUCKET_SELECT_IMAGE_CODE) {
				//reset
				//addMedia(mSelectedImage, data.getStringArrayListExtra("list"));
				addMediaModel(mSelectedImages, data.getParcelableArrayListExtra("selectedImages"));
				
				if (mImageFragment != null) {
					mImageFragment.setCurrentSelectedImages(mSelectedImages);
				}
				
				onImageSelectedCount(mSelectedImages.size());

			} else if (requestCode == MediaChooserConstants.BUCKET_SELECT_VIDEO_CODE) {
				//reset
				//addMedia(mSelectedVideo, data.getStringArrayListExtra("list"));
				addMediaModel(mSelectedVideos, data.getParcelableArrayListExtra("selectedVideos"));
				
				if (mVideoFragment != null) {
					mVideoFragment.setCurrentSelectedVideos(mSelectedVideos);
				}
				
				onVideoSelectedCount(mSelectedVideos.size());

			} else if (requestCode == MediaChooserConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
				
				final AlertDialog alertDialog = MediaChooserConstants.getDialog(BucketHomeFragmentActivity.this).create();
				alertDialog.show();

				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// Do something after 2000ms
						String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();

                        if (mImageFragment != null) {
							mImageFragment.getAdapter().addLatestEntry(fileUriString);
							mImageFragment.getAdapter().notifyDataSetChanged();
						}
						alertDialog.dismiss();
					}
				}, 5000);

			} else if (requestCode == MediaChooserConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {

				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));

				final AlertDialog alertDialog = MediaChooserConstants.getDialog(BucketHomeFragmentActivity.this).create();
				alertDialog.show();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// Do something after 2000ms
						String fileUriString = fileUri.toString().replaceFirst("file:///", "/").trim();

                        if (mVideoFragment != null) {
							mVideoFragment.getAdapter().addLatestEntry(fileUriString);
							mVideoFragment.getAdapter().notifyDataSetChanged();
						}
						
						//add to mediastore
						//addVideoToMediaStore(fileUri);
						
						alertDialog.dismiss();
					}
				}, 5000);
			}
		}
	}

	/*
	private Uri getVideoInMediaStore(Uri videoUri) {
		
		String photoName = videoUri.getLastPathSegment();
		
		ContentResolver contentResolver = this.getContentResolver();
		try {
			//MediaStore.Images.Media.insertImage(contentResolver, destFile.getAbsolutePath(), destFile.getName(), "Jepret Story photos");
			
			ContentValues values = new ContentValues();
			values.put(Video.Media.TITLE, photoName);
			values.put(Video.Media.DISPLAY_NAME, photoName);
			values.put(Video.Media.MIME_TYPE, "video/mp4");
			values.put(Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaChooserConstants.folderName);
			
			return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
			
		} catch (Exception ex) {
			
		}
		
		return null;
	}
	*/
	
	/*
	private void addVideoToMediaStore(Uri videoUri) {
		
		String photoName = videoUri.getLastPathSegment();
		String fileUriString = videoUri.toString().replaceFirst("file:///", "/").trim();
		File destFile = new File(fileUriString);
		
		ContentResolver contentResolver = this.getContentResolver();
		try {
			//MediaStore.Images.Media.insertImage(contentResolver, destFile.getAbsolutePath(), destFile.getName(), "Jepret Story photos");
			
			ContentValues values = new ContentValues();
			values.put(Video.Media.TITLE, photoName);
			values.put(Video.Media.DISPLAY_NAME, photoName);
			//values.put(Images.Media.DESCRIPTION, mCurrentPhoto.getSubscription().getSubsName() + " Photo");
			values.put(Video.Media.MIME_TYPE, "video/mp4");
			//values.put(Images.Media.DATE_ADDED, mCurrentPhoto.getTimestamp().longValue()*1000);
			//values.put(Images.Media.DATE_TAKEN, mCurrentPhoto.getTimestamp().longValue()*1000);
			
//			values.put(Images.ImageColumns.BUCKET_ID, "Jepret Story".hashCode());
			values.put(Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaChooserConstants.folderName);
			values.put(Video.Media.SIZE, destFile.length());
			values.put(Video.Media.DATA, destFile.getAbsolutePath());
			
			contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Video is saved to gallery", Toast.LENGTH_SHORT).show();
				}
			});				
			
		} catch (Exception ex) {
			
		}
	}
	*/
	
	/*
	private void addMedia(ArrayList<String> existing, ArrayList<String> source) {
//		for (String string : source) {
//			//Log.i("Media Chooser", string);
//			if (source.contains(string)) {
//				
//			}
//			else {
//				source.add(string);
//			}
//		}
		
		existing.clear();
		existing.addAll(source);
	}
	*/
	
	private void addMediaModel(ArrayList<MediaModel> existing, ArrayList<Parcelable> source) {
		
		List<MediaModel> newList = new ArrayList<MediaModel>(source.size());
		for (Parcelable model : source) {
			newList.add((MediaModel)model);
			
			if (!existing.contains(model)) {
				//add to media selected view
				onMediaSelected((MediaModel)model);
			}
		}
		for (MediaModel model : existing) {
			if (!newList.contains(model)) {
				//remove from media selected view
				onMediaUnselected(model);
			}
		}
		
		existing.clear();
		existing.addAll(newList);
	}
	
	private void onMediaSelected(MediaModel media) {
		
		View rootView = LayoutInflater.from(BucketHomeFragmentActivity.this).inflate(R.layout.list_item_selected_thumbnail, null);
		ImageView thumbnail = (ImageView) rootView.findViewById(R.id.selected_photo);
		rootView.setTag(media.url);
		rootView.setOnClickListener(this);
		
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
		thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));
		
		if(media.mediaMode == MediaType.VIDEO){
			new VideoLoadAsync(getVideoFragment(), thumbnail, false, px, px).executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, media.url);

		} else {
			ImageLoadAsync loadAsync = new ImageLoadAsync(this, thumbnail, px);
			loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, media.url);
		}
		
		mSelectedImagesContainer.addView(rootView, 0);
		
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

        if (mSelectedImagesContainer.getChildCount() == 0) {
			mSelectedImagesContainer.setVisibility(View.GONE);
			mSelectedImageEmptyMessage.setVisibility(View.VISIBLE);
		}
	}
	
	private void onVideoSelectedCount(int count){
		
		String text = count > 0? (getResources().getString(R.string.videos_tab) + " (" + count + ")"): getResources().getString(R.string.video);
		setTabText(MediaType.VIDEO, text);
	}
	
	private void onImageSelectedCount(int count){
		
		String text = count > 0? (getResources().getString(R.string.images_tab) + " (" + count + ")"): getResources().getString(R.string.image);
		setTabText(MediaType.IMAGE, text);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v instanceof FrameLayout) {
			//deselect
			String url = (String)v.getTag();
			MediaModel model = null;
			if (mSelectedImages != null) {
				for(MediaModel m: mSelectedImages) {
					if (m.url.equals(url)) {
						model = m;
						break;
					}
				}
			}
			if (model == null && mSelectedVideos != null) {
				for(MediaModel m: mSelectedVideos) {
					if (m.url.equals(url)) {
						model = m;
						break;
					}
				}
			}
			if (model != null) {
				if (model.isVideo()) {
					mSelectedVideos.remove(model);
					onVideoSelectedCount(mSelectedVideos.size());
				} else {
					mSelectedImages.remove(model);
					onImageSelectedCount(mSelectedImages.size());
				}
				onMediaUnselected(model);
			}
		}
	}
}
