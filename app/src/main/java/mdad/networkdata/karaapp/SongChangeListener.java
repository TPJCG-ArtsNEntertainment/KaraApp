package mdad.networkdata.karaapp;

// Define an interface for listening to changes in the currently playing song
public interface SongChangeListener
{
    // Method to be called when the current song changes, passing the new position
    void onChanged(int position);
}
