package com.example.aimp.models;

public class Artist {

    public final long id;
    public final String artName;
    public final int albumCount;
    public final int songCont;

    public Artist() {
        id = -1;
        artName = "";
        albumCount = -1;
        songCont = -1;
    }

    public Artist(long id, String artName, int albumCount, int songCont) {
        this.id = id;
        this.artName = artName;
        this.albumCount = albumCount;
        this.songCont = songCont;
    }
}
