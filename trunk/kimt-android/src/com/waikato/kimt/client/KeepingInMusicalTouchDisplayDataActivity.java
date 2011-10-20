package com.waikato.kimt.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.waikato.kimt.KIMTClient;
import com.waikato.kimt.R;
import com.waikato.kimt.greenstone.GreenstoneMusicLibrary;
import com.waikato.kimt.greenstone.MusicSheet;
import com.waikato.kimt.greenstone.MusicSheet.MetaDataDownloadListener;
import com.waikato.kimt.greenstone.MusicView;
import com.waikato.kimt.sync.MusicalDataFrame;
import com.waikato.kimt.sync.MusicalSyncClient;
import com.waikato.kimt.sync.SyncedLibraryUpdateListener;

public class KeepingInMusicalTouchDisplayDataActivity extends Activity {
	/* http://www.codeshogun.com/blog/2009/04/16/how-to-implement-swipe-action-in-android/ */
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 250;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Locate the SensorManager using Activity.getSystemService
		super.onCreate(savedInstanceState);    
		this.setContentView(R.layout.gsdisplay);

		final ImageView imageSheet = (ImageView) findViewById(R.id.imageSheet);
		final TextView formattedText = (TextView) findViewById(R.id.textViewFormatted);
		final ScrollView scrollView = (ScrollView) findViewById(R.id.imageScrollView);

		KIMTClient kimtClient = (KIMTClient) getApplication();

		final MusicalSyncClient musicalSyncClient = kimtClient.getSyncClient();
		final GreenstoneMusicLibrary greenstoneMusicLibrary = kimtClient.getLibrary();

		if (musicalSyncClient == null || greenstoneMusicLibrary == null) {
			onBackPressed();
			return;
		}

		//final ScrollView scrollView = (ScrollView) findViewById(R.id.imageScrollView);

		// Get the extra data bundled with this activities
		// intent
		Bundle bundle = this.getIntent().getExtras();
		boolean isLeader = bundle.getBoolean("is_leader");

		if (isLeader) { 
			// Unserialize the MusicSheet that was sent in the bundle,
			// this MusicSheet was the sheet that was selected earlier.
			final MusicSheet selectedSheet = (MusicSheet) bundle.getSerializable("selected_sheet");

			formattedText.setText(selectedSheet.toString());
			selectedSheet.setOnImageDownloadedListener(new MusicSheet.ImageDataDownloadListener() {
				@Override
				public void onImageChanged(MusicSheet ms) {
					imageSheet.setLayoutParams(new ScrollView.LayoutParams(800, 1280));
					imageSheet.setImageBitmap(ms.getBitmap());
				}
			});
			
			final GestureDetector gs = new GestureDetector(new PageTurnDetector(selectedSheet, musicalSyncClient));
			
			imageSheet.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gs.onTouchEvent(event);
				}
			});


			// Set the bitmap from the internet ..
			selectedSheet.setBitmapFromInternet(0);
		} else {
			formattedText.setText("Status:\n\tWaiting for the conductor to select a sheet ..");
			musicalSyncClient.setOnSyncUpdateListener(new SyncedLibraryUpdateListener() {

				@Override
				public void onSyncViewUpdate(MusicView mv) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onSyncUploaded(Boolean uploaded) {
					// TODO Auto-generated method stub
				}
				@Override
				public void onMusicalDataFrameUpdated(MusicalDataFrame mdf) {
					final GreenstoneMusicLibrary gml;
					MusicSheet currentSheet;
					
					boolean isNewSheet = false;
					
					try {
						String previousSheetID = musicalSyncClient.getDataFrame().getSheetID();
						
						isNewSheet = (previousSheetID.compareTo(mdf.getSheetID()) != 0);
					} catch (NullPointerException ex) {
						isNewSheet = true;
					}
					
					gml	= new GreenstoneMusicLibrary(mdf.getLibraryLocation());
					currentSheet = gml.getCurrentSheet();
					
					if (isNewSheet || gml.getCurrentSheet() == null) {
						gml.setCurrentSheet(mdf.getSheetID());
						currentSheet = gml.getCurrentSheet();
						
						formattedText.setText("Getting metadata for sheet ..");
						
						currentSheet.setOnSheetMetaDataUpdateListener(new MetaDataDownloadListener() {
							@Override
							public void onMetaDataDownloaded(MusicSheet ms) {
								formattedText.setText(ms.toString());
							}
						});

						currentSheet.setOnImageDownloadedListener(new MusicSheet.ImageDataDownloadListener() {
							@Override
							public void onImageChanged(MusicSheet ms) {
								imageSheet.setLayoutParams(new ScrollView.LayoutParams(800, 1280));
								imageSheet.setImageBitmap(ms.getBitmap());
							}
						});
					}
					
					// Set the bitmap from the internet ..
					currentSheet.setBitmapFromInternet(mdf.getPage());
					Toast.makeText(getApplicationContext(), "Getting page " + Integer.toString(mdf.getPage()), Toast.LENGTH_SHORT).show();
				}
			});

			Toast.makeText(getApplicationContext(), "Waiting for the conductor...", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBackPressed() {
		KIMTClient kimtClient = (KIMTClient) getApplication();
		MusicalSyncClient musicalSyncClient = kimtClient.getSyncClient();
		if (musicalSyncClient.isLeader()) {
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Can not load list item. You are not the conductor.", Toast.LENGTH_SHORT).show();
		}

		return;
	}
	
	private class PageTurnDetector extends SimpleOnGestureListener {
		private int currentPage = 0;
		
		private MusicSheet selectedSheet;
		private MusicalSyncClient musicalSyncClient;
		
		public PageTurnDetector(MusicSheet selectedSheet, MusicalSyncClient musicalSyncClient) {
			this.selectedSheet = selectedSheet;
			this.musicalSyncClient = musicalSyncClient;
			
			Log.v("KeepingInMusicalTouch", "Created PageTurnDetector" + selectedSheet.toString() + " " + musicalSyncClient.toString());
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			MusicalDataFrame newMusicalDataFrame = musicalSyncClient.getDataFrame(); 
			
			Log.v("KeepingInMusicalTouch", "onFling");
			Toast.makeText(getApplicationContext(), "onFling", Toast.LENGTH_SHORT).show();
			
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// left swipe
					Toast.makeText(getApplicationContext(), "LS/Setting page " + Integer.toString(currentPage), Toast.LENGTH_SHORT).show();
					
					if (currentPage > 0) {
						selectedSheet.setBitmapFromInternet(--currentPage);
						newMusicalDataFrame.setPage(currentPage);
						musicalSyncClient.setMusicalDataFrame(newMusicalDataFrame);
						
						
						return true;
					}
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// right swipe
					Toast.makeText(getApplicationContext(), "RS/Setting page " + Integer.toString(currentPage), Toast.LENGTH_SHORT).show();
					
					if ((currentPage + 1) < selectedSheet.getNumberOfPages()) {
						selectedSheet.setBitmapFromInternet(++currentPage);
						newMusicalDataFrame.setPage(currentPage);
						musicalSyncClient.setMusicalDataFrame(newMusicalDataFrame);
						
						return true;
					}
				}
			} catch (Exception e) {
				// nothing
			}

			return false;
		}
	}
}
