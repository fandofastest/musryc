package com.pstyr.msrlte;

public class TrackView {

    private String songTitle, songUri, artworkUrl, user, duration,id;

    public TrackView(String songTitle, String songUri, String artworkUrl, String user, String duration, String likes_count, String id) {
        this.songTitle = songTitle;
        this.songUri = songUri;
        this.artworkUrl = artworkUrl;
        this.user = user;
        this.duration = duration;
        this.id=id  ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongUri() {
        return songUri;
    }

    public void setSongUri(String songUri) {
        this.songUri = songUri;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getduration() {
        return duration;
    }

    public void setduration(String duration) {
        this.duration = duration;
    }
}
