package com.waikato.kimt;

import java.util.ArrayList;
import java.util.List;

public class GreenstoneMusicLibrary implements MusicLibrary {
	private MusicSheet			current;

	private String				trackUri;
	private DigitalLibrarySync	dls;
	
	/**
	 * Will connect to the specified Greenstone music library
	 * at the specified URI.
	 * @param uri
	 * 	The URI to connect to. It's expected to be a Greenstone3 server.
	 */
	public GreenstoneMusicLibrary(String uri) {
		this.connect(uri);
	}
	
	@Override
	public void connect(String uri) {
		this.trackUri = uri;
	}

	public String getUri() {
		return trackUri;
	}
	
	public DigitalLibrarySync getSyncer() {
		return null;
	}
	
	@Override
	public void setCurrentSheet(String sheetID) {
		this.current = new MusicSheet(this, sheetID);
		this.current.setOnSheetMetaDataUpdateListener(new MusicSheet.MetaDataDownloadListener() {
			
			@Override
			public void onMetaDataDownloaded(MusicSheet ms) {
				notifySheetMetaDataUpdate();
			}
		});
	}

	@Override
	public MusicSheet getCurrentSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MusicSheet find(String searchTerm, SearchMode sm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MusicView getCurrentView() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<SyncedSheetUpdateListener>
		registeredMetaUpdateListeners = new ArrayList<SyncedSheetUpdateListener>();
	
	public void setOnSheetMetaDataUpdateListener(SyncedSheetUpdateListener gul) {
		registeredMetaUpdateListeners.add(gul);
	}
	
	private void notifySheetMetaDataUpdate() {
		for (SyncedSheetUpdateListener g : registeredMetaUpdateListeners) {
			g.onMetaDataUpdate(current);
		}
	}
	
	public interface SyncedSheetUpdateListener {
		public void onMetaDataUpdate(MusicSheet m);
	}

}
