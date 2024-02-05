package mdad.networkdata.karaapp;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Player extends Fragment implements SongChangeListener {
    //    Initialization parameters for fragment
    private final List<MusicList> musicLists= new ArrayList<>();
    private RecyclerView musicRecyclerView;
    private MediaPlayer mediaPlayer;
    private TextView endTime,startTime;
    private boolean isPlaying=false;
    private SeekBar playerSeekBar;
    private ImageView playPauseImg;
    private Timer timer;
    private MusicAdapter musicAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isUserInteractingWithSeekBar = false;
    private int currentSongListPosition=0;
    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
    private String mParam1, mParam2;
    //    Empty public constructor for fragment
    public Player(){};
    //    Create new instance of fragment with this method with parameters
    public static Player newInstance(String param1, String param2)
    {
        Player player = new Player();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        player.setArguments(args);
        return player;
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_player, container, false);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //    ------ Beginning of fragment customization ------
//    Declaration of String for selected item and Boolean for fullscreen and power status
        //    Declaration of components from Player's xml
        musicRecyclerView = view.findViewById(R.id.musicRecyclerView);
        final CardView playPauseCard = view.findViewById(R.id.playPauseCard);
        playPauseImg= view.findViewById(R.id.playPauseImg);

        final ImageView nextBtn = view.findViewById(R.id.nextBtn);
        final ImageView previousBtn = view.findViewById(R.id.previousBtn);

        Button playlist = view.findViewById(R.id.btnPlaylist);

        startTime= view.findViewById(R.id.startTime);
        endTime=view.findViewById(R.id.endTime);
        playerSeekBar=view.findViewById(R.id.playerSeekBar);

        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mediaPlayer=new MediaPlayer();

        // Check if the permission to read external storage has been granted
        // If permission is granted, call getMusicFiles() to access music files
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
        {getMusicFiles();}
        else
        {// If permission is not granted, check the Android version
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {// If running on Android Marshmallow (API level 23) or higher
                // Request the permission to read external storage
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);
                // Print "Test2" to the console for debugging purposes
                System.out.println("Test2");
                // Call getMusicFiles() to attempt accessing music files after requesting permission

                getMusicFiles();
            }
            // If running on a version lower than Android Marshmallow (API level 23)
            // No explicit permission request is needed, so proceed to call getMusicFiles()
            else
            {getMusicFiles();}
        }

        playPauseCard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // Check if the media player is currently playing audio
                if(isPlaying) {
                    // Set the flag indicating playback is paused
                    isPlaying=false;
                    // Pause the media player
                    mediaPlayer.pause();
                    // Change the icon to represent pause state
                    playPauseImg.setImageResource(R.drawable.play_icon);
                } else {
                    // Set the flag indicating playback is resumed
                    isPlaying=true;
                    // Start or resume the media player
                    mediaPlayer.start();
                    // Change the icon to represent play state
                    playPauseImg.setImageResource(R.drawable.pause_btn);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Calculate the index of the next song in the list
                int nextSongListPosition= currentSongListPosition+1;
                // If the calculated index exceeds the size of the music list, reset it to 0
                // This ensures that the music list loops back to the start when it reaches the end
                if(nextSongListPosition>=musicLists.size())
                {
                    nextSongListPosition=0;

                }
                // Check if the music list is empty
                if(musicLists.size() == 0)
                {// Display a toast message indicating that there is no music in the list
                    Toast.makeText(getActivity(),"No music in list",Toast.LENGTH_SHORT).show();
                }
                else
                {// If there is music in the list, perform the following actions
                    // First, mark the current song as not playing
                    musicLists.get(currentSongListPosition).setPlaying(false);
                    // Then, mark the next song as playing
                    musicLists.get(nextSongListPosition).setPlaying(true);

                    // Update the music list adapter with the new list
                    musicAdapter.updateList(musicLists);
                    // Scroll the RecyclerView to show the next song
                    musicRecyclerView.scrollToPosition(nextSongListPosition);
                    // Call the 'onChanged' method with the index of the next song
                    onChanged(nextSongListPosition);
                }

            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Calculate the index of the previous song in the list
                int prevSongListPosition= currentSongListPosition-1;
                // If the calculated index is less than 0, reset it to the last index in the music list
                // This ensures that the music list loops back to the end when it reaches the beginning
                if(prevSongListPosition<0)
                {
                    prevSongListPosition=musicLists.size()-1;//play last song
                }
                if(musicLists.size() == 0)
                {// Display a toast message indicating that there is no music in the list
                    Toast.makeText(getActivity(),"No music in list",Toast.LENGTH_SHORT).show();
                }
                else
                {// If there is music in the list, perform the following actions
                    // First, mark the current song as not playing
                    musicLists.get(currentSongListPosition).setPlaying(false);
                    // Then, mark the previous song as playing
                    musicLists.get(prevSongListPosition).setPlaying(true);

                    // Update the music list adapter with the new list
                    musicAdapter.updateList(musicLists);
                    // Scroll the RecyclerView to show the previous song
                    musicRecyclerView.scrollToPosition(prevSongListPosition);
                    // Call the 'onChanged' method with the index of the previous song
                    onChanged(prevSongListPosition);
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {// Check if the user is interacting with the SeekBar
                if(fromUser)
                {// Indicate that the user is actively changing the SeekBar's progress
                    isUserInteractingWithSeekBar = true;
                    // If the media is currently playing
                    if(isPlaying)
                    {// Move the media playback to the new position indicated by the SeekBar's progress

                        mediaPlayer.seekTo(progress);
                    } else {// If the media is not playing, move the playback to the beginning
                        mediaPlayer.seekTo(0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // When the user starts tracking the SeekBar (e.g., touches it), indicate interaction
                isUserInteractingWithSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // When the user stops tracking the SeekBar (e.g., lifts their finger), indicate no interaction
                isUserInteractingWithSeekBar = false;
            }
        });


        playlist.setOnClickListener(new View.OnClickListener()
        {// When the playlist button is clicked, call the method to replace the current fragment
            // with another one. This is typically done to switch between different views or screens.
            @Override
            public void onClick(View v) {
                replaceFragmentWithAnother();
            }
        });

    }

    public void replaceFragmentWithAnother()
    {
        MusicPlaylist anotherFragment = new MusicPlaylist();

        // Begin the transaction
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // Replace whatever is in the container view with this fragment
        transaction.replace(R.id.fragment_container, anotherFragment);

        // Commit the transaction
        transaction.commit();
    }



    private void getMusicFiles() {
        // Obtain a ContentResolver instance to query the content provider
        ContentResolver contentResolver = getActivity().getContentResolver();
        // Define the URI for accessing the internal storage where music files are usually stored
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // Query the content resolver to retrieve metadata about all MP3 files
        Cursor cursor = contentResolver.query(uri, null,MediaStore.Audio.Media.DATA+" LIKE?",new String[]{"%.mp3%"},null);
        // Check if the cursor returned from the query is null, which means the query failed
        if(cursor==null) {
            Toast.makeText(getActivity(),"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        // If the cursor does not have any rows, meaning no music files were found, display a message
        else if (!cursor.moveToNext()) {
            Toast.makeText(getActivity(),"No Music Found",Toast.LENGTH_SHORT).show();
            // If music files are found, iterate over them and create MusicList objects
        } else {
            while(cursor.moveToNext()) {
                // Retrieve column indices for the title, ID, and artist
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

                // Extract data from the cursor for the file name, artist name, and ID
                final String getMusicFileName = cursor.getString(titleIndex);
                final String getArtistName = cursor.getString(artistIndex);
                long cursorId = cursor.getLong(idIndex);

                // Create a Uri for the music file using the ID
                Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);
                String getDuration="00:00";

                // If running on Android Q or later, also retrieve the duration of the track
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
                    int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION);
                    getDuration=cursor.getString(durationIndex);
                }

                // Create a MusicList object with the extracted data and add it to the list
                final MusicList musicList= new MusicList(getMusicFileName,getArtistName,getDuration,false,musicFileUri);
                musicLists.add(musicList);
            }

            // After processing all music files, check if the fragment is still added to the activity
            // If so, create a new MusicAdapter and set it to the RecyclerView
            if (isAdded()) {
                musicAdapter= new MusicAdapter(musicLists,this);
               musicRecyclerView.setAdapter(musicAdapter);
            }
        }
        // Close the cursor after processing to free up resources

        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length> 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            // Check if the requestCode matches the one used when requesting permissions
            // Check if the first result (usually the only one) indicates that the permission was granted
            getMusicFiles();
        } else {
            // If the permission was not granted, notify the user that the permission declined is causing issues

            Toast.makeText(getActivity(),"Permission Declined By User",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChanged(int position) {
        // Update the current song position in the list
        currentSongListPosition=position;
        // If the media player is currently playing a song
        if(mediaPlayer.isPlaying()) {
            // Pause the media player and reset it to prepare for the next song
            mediaPlayer.pause();
            mediaPlayer.reset();
        } else {
            // If the media player is not playing, just reset it
            mediaPlayer.reset();
        }
        // Set the audio stream type to music
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Start a new thread to prepare the media player asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Ensure the fragment is still attached to the activity
                    if (isAdded()) {
                        // Set the data source for the media player to the selected song's file URI
                        mediaPlayer.setDataSource(getActivity(), musicLists.get(position).getMusicFile());
                        // Prepare the media player
                        mediaPlayer.prepare();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // If the fragment is still attached to the activity, show a toast message
                    if (isAdded()) {
                        Toast.makeText(getActivity(), "Unable to play track", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).start();

        // Set an on prepared listener to the media player
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // Calculate the total duration of the song in minutes and seconds
                final int getTotalDuration = mp.getDuration();
                String generateDuration = String.format(Locale.getDefault(),"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getTotalDuration),
                        TimeUnit.MILLISECONDS.toSeconds(getTotalDuration)-
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTotalDuration)));

                // Set the end time text view to the total duration
                endTime.setText(generateDuration);
                // Set the isPlaying flag to true since the song is now ready to play
                isPlaying=true;
                // Start the media player
                mp.start();
                // Set the maximum progress of the seek bar to the total duration of the song
                playerSeekBar.setMax(getTotalDuration);
                // Change the play/pause button image to the pause icon
                playPauseImg.setImageResource(R.drawable.pause_btn);
            }
        });

        // Schedule a recurring task to update the seek bar and start time every second
        timer=new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Get the current position of the media player

                        final int getCurrentDuration = mediaPlayer.getCurrentPosition();
                        // Format the current position into minutes and seconds
                        String generateDuration = String.format(Locale.getDefault(),"%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration),
                                TimeUnit.MILLISECONDS.toSeconds(getCurrentDuration)-
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration)));

                        // Update the seek bar's progress to the current position
                        playerSeekBar.setProgress(getCurrentDuration);
                        // Set the start time text view to the current position
                        startTime.setText(generateDuration);
                    }
                });

                // Set an on completion listener to the media player
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    // Only handle completion if the user did not interact with the seek bar
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!isUserInteractingWithSeekBar)
                        {
                            // Reset the media player and cancel the timer
                            mediaPlayer.reset();
                            timer.purge();
                            timer.cancel();
                            // Set the isPlaying flag to false since the song has finished playing
                            isPlaying = false;
                            // Change the play/pause button image to the play icon
                            playPauseImg.setImageResource(R.drawable.play_icon);
                            // Reset the seek bar's progress to 0
                            playerSeekBar.setProgress(0);
                            // Increment the current song position
                            currentSongListPosition++;
                            // Update the music list adapter with the new list
                            musicAdapter.updateList(musicLists);
                            // Scroll the RecyclerView to show the next song
                            musicRecyclerView.scrollToPosition(currentSongListPosition);

                            // If the current song position is valid
                            if (currentSongListPosition < musicLists.size()) {
                                // Set the audio stream type to music
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                                // Start a new thread to prepare the media player for the next song
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // Ensure the fragment is still attached to the activity
                                            if (isAdded()) {
                                                // Set the data source for the media player to the next song's file URI
                                                mediaPlayer.setDataSource(getActivity(), musicLists.get(currentSongListPosition).getMusicFile());
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            // If the fragment is still attached to the activity, show a toast message
                                            if (isAdded()) {
                                                Toast.makeText(getActivity(), "Unable to play track", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }).start();
                            } else {
                                // If the current song position is beyond the last song in the list, reset the position to 0
                                // This allows for looping through the playlist from the beginning                                currentSongListPosition = 0;
                            }
                        }
                    }
                });
            }
        },1000,1000);
    }
}