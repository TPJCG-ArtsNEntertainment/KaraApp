package mdad.networkdata.karaapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Time;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
// Define a custom adapter for a RecyclerView to display a list of music tracks
public class MusicAdapter extends  RecyclerView.Adapter<MusicAdapter.MyViewHolder>
{
    private  List<MusicList> list;
    //private final Context context;
    private int playingPosition =0;
    private final SongChangeListener songChangeListener;

    // Constructor for the MusicAdapter, takes the list of music tracks and a listener for song changes
    public MusicAdapter(List<MusicList>list, SongChangeListener songChangeListener)
    {
        this.list=list;
        this.songChangeListener = songChangeListener;
    }

    // Override the onCreateViewHolder method to inflate the view for each item in the RecyclerView
    @NonNull
    @Override
    public MusicAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_adapter_layout,null));
    }
    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.MyViewHolder holder, int position)
    {
        MusicList list2 = list.get(position);
        // Set the background color of the root layout based on whether the track is playing
        if(list2.isPlaying())
        {
            playingPosition=position;
            holder.rootLayout.setBackgroundColor(Color.GRAY);
        }
        else
        {
            holder.rootLayout.setBackgroundColor(Color.WHITE);
        }
        // Generate a formatted string representing the track's duration
        String generateDuration = String.format(Locale.getDefault(),"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(list2.getDuration())),
                TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(list2.getDuration()))-
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(list2.getDuration()))));

        // Set the text views with the track's details
        holder.title.setText(list2.getTitle());
        holder.artist.setText(list2.getArtist());
        holder.musicDuration.setText(generateDuration);

        // Set an OnClickListener for the root layout to handle track selection
        holder.rootLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // When a track is clicked, stop playing the current track and start playing the selected one
                list.get(playingPosition).setPlaying(false);
                list2.setPlaying(true);
                songChangeListener.onChanged(position); // Notify the listener that the track has changed
                notifyDataSetChanged();// Refresh the RecyclerView to reflect the new state
            }
        });

    }

    // Method to update the list of music tracks and refresh the RecyclerView
    public void updateList (List<MusicList>list)
    {
        this.list=list;
        notifyDataSetChanged();
    }

    // Override the getItemCount method to return the size of the music list
    @Override
    public int getItemCount()
    {
        return list.size();
    }

    // Static inner class defining the ViewHolder for the RecyclerView items
    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rootLayout;// Root layout of the item
        private final TextView title;// TextView for the track title
        private final TextView artist; // TextView for the track artist
        private final TextView musicDuration;// TextView for the track duration

        // Constructor for the ViewHolder, finds and holds references to the views
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            rootLayout= itemView.findViewById(R.id.rootLayout);
            title= itemView.findViewById(R.id.musicTitle);
            artist= itemView.findViewById(R.id.musicArtist);
            musicDuration= itemView.findViewById(R.id.musicDuration);

        }
    }
}
