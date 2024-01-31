package mdad.networkdata.karaapp;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
    private int currentSongListPosition=0;
    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
    private String mParam1, mParam2;
    public Player(){};
    public static Player newInstance(String param1, String param2) {
        Player player = new Player();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        player.setArguments(args);
        return player;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_player, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
//        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
        //Hides Status Bar and Navigation
        if (activity != null) {
            View decodeView = activity.getWindow().getDecorView();
            int options =  View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decodeView.setSystemUiVisibility(options);
        }
        musicRecyclerView = view.findViewById(R.id.musicRecyclerView);
        final CardView playPauseCard = view.findViewById(R.id.playPauseCard);
        playPauseImg= view.findViewById(R.id.playPauseImg);

        final ImageView nextBtn = view.findViewById(R.id.nextBtn);
        final ImageView previousBtn = view.findViewById(R.id.previousBtn);

        startTime= view.findViewById(R.id.startTime);
        endTime=view.findViewById(R.id.endTime);
        playerSeekBar=view.findViewById(R.id.playerSeekBar);

        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mediaPlayer=new MediaPlayer();

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
        {
            System.out.println("Test1");
            getMusicFiles();
        } else {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);
                System.out.println("Test2");
                getMusicFiles();
            }
            else {
                System.out.println("Test3");
                getMusicFiles();
            }
        }

        playPauseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    isPlaying=false;
                    mediaPlayer.pause();
                    playPauseImg.setImageResource(R.drawable.play_icon);
                } else {
                    isPlaying=true;
                    mediaPlayer.start();
                    playPauseImg.setImageResource(R.drawable.pause_btn);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextSongListPosition= currentSongListPosition+1;

                if(nextSongListPosition>=musicLists.size()) {
                    nextSongListPosition=0;
                }

                musicLists.get(currentSongListPosition).setPlaying(false);
                musicLists.get(nextSongListPosition).setPlaying(true);

                musicAdapter.updateList(musicLists);
                musicRecyclerView.scrollToPosition(nextSongListPosition);
                onChanged(nextSongListPosition);
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int prevSongListPosition= currentSongListPosition-1;

                if(prevSongListPosition<0) {
                    prevSongListPosition=musicLists.size()-1;//play last song
                }

                musicLists.get(currentSongListPosition).setPlaying(false);
                musicLists.get(prevSongListPosition).setPlaying(true);

                musicAdapter.updateList(musicLists);
                musicRecyclerView.scrollToPosition(prevSongListPosition);
                onChanged(prevSongListPosition);
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    if(isPlaying) {
                        mediaPlayer.seekTo(progress);
                    } else {
                        mediaPlayer.seekTo(0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void getMusicFiles() {
        ContentResolver contentResolver = getActivity().getContentResolver();
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
//        Cursor cursor = contentResolver.query(uri, null,MediaStore.Audio.Media.DATA+" LIKE?",new String[]{"%.mp3%"},null);
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if(cursor==null) {
            Toast.makeText(getActivity(),"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        else if (!cursor.moveToNext()) {
            Toast.makeText(getActivity(),"No Music Found",Toast.LENGTH_SHORT).show();
        } else {
            while(cursor.moveToNext()) {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

                final String getMusicFileName = cursor.getString(titleIndex);
                final String getArtistName = cursor.getString(artistIndex);
                long cursorId = cursor.getLong(idIndex);

                Uri musicFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);
                String getDuration="00:00";

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
                    int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION);
                    getDuration=cursor.getString(durationIndex);
                }

                final MusicList musicList= new MusicList(getMusicFileName,getArtistName,getDuration,false,musicFileUri);
                musicLists.add(musicList);
            }

            if (isAdded()) {
                musicAdapter= new MusicAdapter(musicLists,this);
               musicRecyclerView.setAdapter(musicAdapter);
            }
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(grantResults.length> 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            getMusicFiles();
        } else {
            Toast.makeText(getActivity(),"Permission Declined By User",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChanged(int position) {
        currentSongListPosition=position;
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.reset();
        } else {
            mediaPlayer.reset();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isAdded()) {
                        mediaPlayer.setDataSource(getActivity(), musicLists.get(position).getMusicFile());
                        mediaPlayer.prepare();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (isAdded()) {
                        Toast.makeText(getActivity(), "Unable to play track", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).start();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                final int getTotalDuration = mp.getDuration();
                String generateDuration = String.format(Locale.getDefault(),"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getTotalDuration),
                        TimeUnit.MILLISECONDS.toSeconds(getTotalDuration)-
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTotalDuration)));

                endTime.setText(generateDuration);
                isPlaying=true;
                mp.start();

                playerSeekBar.setMax(getTotalDuration);
                playPauseImg.setImageResource(R.drawable.pause_btn);
            }
        });

        timer=new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int getCurrentDuration = mediaPlayer.getCurrentPosition();
                        String generateDuration = String.format(Locale.getDefault(),"%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration),
                                TimeUnit.MILLISECONDS.toSeconds(getCurrentDuration)-
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration)));

                        playerSeekBar.setProgress(getCurrentDuration);
                        startTime.setText(generateDuration);
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.reset();

                        timer.purge();
                        timer.cancel();

                        isPlaying=false;
                        playPauseImg.setImageResource(R.drawable.play_icon);
                        playerSeekBar.setProgress(0);
                    }
                });
            }
        },1000,1000);
    }
}