package mdad.networkdata.karaapp;

import android.net.Uri;

// Define a class to represent a music item in the app
public class MusicList {
    // Define properties for the music item: title, artist, duration, playing status, and file URI
    private String title, artist,duration;
    private boolean isPlaying;
    private Uri musicFile;

    // Constructor for creating a new MusicList object with the given properties
    public MusicList(String title, String artist, String duration, boolean isPlaying,Uri musicFile)
    {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.isPlaying = isPlaying;
        this.musicFile= musicFile;

    }

    // Getter methods to retrieve the properties of the music item
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Uri getMusicFile() {
        return musicFile;
    }

    // Setter method to change the playing status of the music item
    public void setPlaying(boolean playing) {isPlaying = playing;}
}
