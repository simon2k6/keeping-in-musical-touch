package com.waikato.kimt.sync;

import java.io.Serializable;

import com.waikato.kimt.greenstone.MusicView;

public class MusicalDataFrame implements Serializable {
	private MusicView	currentView;
	
	private String	libraryLocation;
	private String	trackLocation;
	
	/**
	 * The serial version of this object. Update when the object changes.
	 */
	private static final long serialVersionUID = 1L;

	public MusicalDataFrame() {
		
	}
	
	public MusicalDataFrame(String libraryLocation) {
		this.libraryLocation = libraryLocation;
	}
	
	public void setLibraryLocation(String location) {
		this.libraryLocation = location;
	}
	
	public String getLibraryLocation() {
		return libraryLocation;
	}
	
	public void setTrackLocation(String location) {
		this.trackLocation = location;
	}
	
	public String getTrackLocation() {
		return trackLocation;
	}

	public void setMusicView(MusicView mv) {
		this.currentView = mv;
	}
	
	public MusicView getMusicView() {
		return this.currentView;
	}
	
	public String toString() {
		return "MusicDataFrame: " + "LibraryLocation: " + getLibraryLocation() + " TrackLocation: " + getTrackLocation();
	}
	

}
