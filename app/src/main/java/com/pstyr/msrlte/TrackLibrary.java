package com.pstyr.msrlte;


public class TrackLibrary {
    private String songTitle, songUrl;

    public TrackLibrary(String songTitle, String songUrl) {
        this.songTitle = songTitle;
        this.songUrl = songUrl;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }
}
