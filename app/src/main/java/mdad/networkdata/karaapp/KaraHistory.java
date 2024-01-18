package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class KaraHistory extends AppCompatActivity {

    Button HisbtnEditMusic, HisbtnPlayedMusic, HisbtnRemoveMusic;
    ListView HislistView;
    YouTubePlayer HisyouTubePlayer;
    LinearLayout HisbuttonLinearLayout;
    ArrayList<HashMap<String, String>> musicsList;
    private boolean isFullscreen = false, is_staffBoolean;
    private int selectedPosition = -1;
    private String HisselectedMusicId, HisselectedMusicName, HisselectedMusicArtist, HisselectedMusicUrl;
    private final int get_all_queue_music = 1, update_delete_music =2, get_power=3, update_power=4;
    // url to get all products list via the php file get_all_productsJson.php
    public static String ipBaseAddress = "http://aetpjcgkara.atspace.cc/";
    private static String url_all_queue_musics = KaraHistory.ipBaseAddress+"get_all_musicHistoryVolley.php";
    private static String url_update_music = KaraHistory.ipBaseAddress+"update_musicVolley.php";
    private static String url_delete_music = KaraHistory.ipBaseAddress+"delete_musicVolley.php";
    private static String url_get_power = KaraHistory.ipBaseAddress+"get_powerVolley.php";
    private static String url_update_power = KaraHistory.ipBaseAddress+"update_powerVolley.php";
    String uid,is_staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kara_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);





        YouTubePlayerView youTubePlayerView = findViewById(R.id.historyPageYoutubePlayer);
        LinearLayout linearLayout = findViewById(R.id.historyPageLinearLayout);
        FrameLayout fullscreenViewContainer = findViewById(R.id.historyPageFullScreenViewContainer);
        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .build();
        youTubePlayerView.setEnableAutomaticInitialization(false);
        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(View fullscreenView, Function0<Unit> exitFullscreen) {
                isFullscreen = true;
                youTubePlayerView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = KaraHistory.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(KaraHistory.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) KaraHistory.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                    }
                }
            }

            @Override
            public void onExitFullscreen() {
                isFullscreen = false;
                youTubePlayerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = KaraHistory.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(KaraHistory.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) KaraHistory.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            }
        });
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                KaraHistory.this.HisyouTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo("7L3Hdp86aio", 0f);
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);


        HislistView = (ListView) findViewById(R.id.listViewHistory);
        // ArrayList to store product info in Hashmap for ListView
        musicsList = new ArrayList<HashMap<String, String>>();
        // re-usable method to use Volley to retrieve products from database
        postData(url_all_queue_musics, null, get_all_queue_music);


        HisbtnEditMusic = (Button) findViewById(R.id.btnHistoryEdit);
        HisbtnEditMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                System.out.println(HisselectedMusicId);
                Intent intent = new Intent(getApplicationContext(), EditMusic.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("mid", HisselectedMusicId);
                startActivity(intent);
            }
        });


        HisbtnPlayedMusic = (Button) findViewById(R.id.btnSetUnPlayed);
        HisbtnPlayedMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid", HisselectedMusicId);
                params_update.put("music_name", HisselectedMusicName);
                params_update.put("artist_name", HisselectedMusicArtist);
                params_update.put("url", HisselectedMusicUrl);
                params_update.put("is_played", "0");
                postData(url_update_music, params_update, update_delete_music);
            }
        });


        HisbtnRemoveMusic = (Button) findViewById(R.id.btnHistoryRemove);
        HisbtnRemoveMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid", HisselectedMusicId);
                postData(url_delete_music, params_update, update_delete_music);
            }
        });

        HisbuttonLinearLayout = findViewById(R.id.HistoryPageButtonLinearLayout);


        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");
        String is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");
        if (!is_staffBoolean) {
            HisbuttonLinearLayout.setVisibility(View.GONE);
            HisbtnEditMusic.setVisibility(View.GONE);
            HisbtnRemoveMusic.setVisibility(View.GONE);
            HisbtnPlayedMusic.setVisibility(View.GONE);
        } else if (is_staffBoolean) {
            HisbuttonLinearLayout.setVisibility(View.VISIBLE);
            HisbtnEditMusic.setVisibility(View.VISIBLE);
            HisbtnRemoveMusic.setVisibility(View.VISIBLE);
            HisbtnPlayedMusic.setVisibility(View.VISIBLE);
        }
        if (HisselectedMusicId == null) {
            HisbtnEditMusic.setClickable(false);
            HisbtnEditMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            HisbtnPlayedMusic.setClickable(false);
            HisbtnPlayedMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            HisbtnRemoveMusic.setClickable(false);
            HisbtnRemoveMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));


        }
    }



    public void postData(String url, Map params, final int requestType) {
        //create a RequestQueue for Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //create a StringRequest for Volley for HTTP Post
        StringRequest stringRequest = new StringRequest( Request.Method.POST, url,
                //response from server
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (requestType == get_all_queue_music) {
                            //check if error code received from server.
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                                return;
                            }
                            //handle the response data received from server
                            //store each product from database records in String array
                            String[] musics = response.split("\\|");
                            // for each product, retrieve the music details
                            for (int i = 0; i < musics.length; i++) {
                                // Storing each product info in variable
                                String[] details = musics[i].split(";");
                                String mid = details[0];
                                String musicName = details[1];
                                String artistName = details[2];
                                String url = details[3];
                                String created_at = details[4];
                                String created_by = details[5];

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();
                                // adding each product info to HashMap key-value pair
                                map.put("music_id", mid);
                                map.put("music_name", musicName);
                                map.put("artist_name", artistName);
                                map.put("url", url);
                                map.put("created_at", created_at);
                                map.put("created_by", created_by);

                                // adding map HashList to ArrayList
                                musicsList.add(map);
                            }
                            //populate the listview with product information from Hashmap
                            ListAdapter adapter = new SimpleAdapter(
                                    KaraHistory.this, musicsList,
                                    R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"}, new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
                            ){
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    final HashMap<String, String> rowData = musicsList.get(position);
                                    ImageView youtubePreviewImage = view.findViewById(R.id.youtubePreviewImage);
                                    String videoId = extractVideoId(rowData.get("url"));
                                    String imageUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                                    Picasso.get().load(imageUrl).into(youtubePreviewImage);
                                    Button btnSelect = view.findViewById(R.id.btnSelect);
                                    btnSelect.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            HisselectedMusicId = rowData.get("music_id");
                                            HisselectedMusicName = rowData.get("music_name");
                                            HisselectedMusicArtist = rowData.get("artist_name");
                                            HisselectedMusicUrl = rowData.get("url");
                                            String videoId = extractVideoId(HisselectedMusicUrl);
                                            HisyouTubePlayer.loadVideo(videoId,1);
                                            Toast.makeText(getApplicationContext(), "Playing: " + HisselectedMusicName, Toast.LENGTH_SHORT).show();

                                            HisbuttonLinearLayout.setVisibility(View.VISIBLE);

                                            HisbtnEditMusic.setClickable(true);
                                            HisbtnEditMusic.setBackgroundColor(0xFF6200EE);
                                            HisbtnPlayedMusic.setClickable(true);
                                            HisbtnPlayedMusic.setBackgroundColor(0xFF6200EE);
                                            HisbtnRemoveMusic.setClickable(true);
                                            HisbtnRemoveMusic.setBackgroundColor(0xFF6200EE);

                                            selectedPosition = position;
                                            notifyDataSetChanged();
                                        }
                                    });
                                    if (position == selectedPosition) {
                                        view.setBackgroundColor(Color.YELLOW);
                                    } else {
                                        view.setBackgroundColor(Color.WHITE);
                                    }
                                    return view;
                                }
                            };
                            // updating listview
                            HislistView.setAdapter(adapter);
                        }
                        if (requestType == update_delete_music) {
                            System.out.println(response);
                            if (response.trim().equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.trim().equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
                                for (int i = 0; i < musicsList.size(); i++) {
                                    HashMap<String, String> map = musicsList.get(i);
                                    String musicId = map.get("music_id");
                                    if (musicId.equals(HisselectedMusicId)) {
                                        musicsList.remove(i);
                                        break;
                                    }

                                }
                                selectedPosition = -1;
                                HisbtnEditMusic.setClickable(false);
                                HisbtnEditMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                HisbtnPlayedMusic.setClickable(false);
                                HisbtnPlayedMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                HisbtnRemoveMusic.setClickable(false);
                                HisbtnRemoveMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                ((BaseAdapter) HislistView.getAdapter()).notifyDataSetChanged();
                            }
                        }


                    }
                },
                //error in Volley
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error
                        Toast.makeText(getApplicationContext(),"Error in retrieving database",Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                //send pid stored in HashMap using HTTP Post in Volley
                return params;
            }
        };
        //add StringRequest to RequestQueue in Volley
        requestQueue.add(stringRequest);
    }

    public String extractVideoId(String url) {
        String videoId = "";
        try {
            // Regular expression pattern to match YouTube video IDs
            Pattern pattern = Pattern.compile("^.*(youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|&v=)([^#&?]*).*");
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches() && matcher.group(2).length() == 11) {
                videoId = matcher.group(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoId;
    }
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (isFullscreen) {
                HisyouTubePlayer.toggleFullscreen();
            } else {
                finish();
            }
        }
    };


    @Override
    //add the option menu to the activity
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the option menu and display the option items when clicked;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        String className = getClass().getSimpleName();
        String[] words = className.split("(?=[A-Z])");
        className = String.join(" ", words).trim();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getTitle().toString().equals(className)) {
                item.setVisible(false);
            }
        }
        if (!is_staffBoolean) {
            menu.findItem(R.id.item5).setVisible(false);
        }
        return true;
    }
    @Override
    //when the option item is selected
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8};
        Class<?>[] destinationClasses = {KaraSession.class, KaraHistory.class, MusicPlayer.class, MusicLyrics.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class, Login.class};
        // Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
            if (id == menuItems[i]) {
                // Start the activity for the selected menu item
                startActivityIntent(destinationClasses[i]);
                return true;
            } else if (id == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        // If the selected item is not found in the loop, fallback to super.onOptionsItemSelected
        return super.onOptionsItemSelected(item);
    }
    private void startActivityIntent(Class<?> cls) {
        Intent intent = new Intent(KaraHistory.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        startActivity(intent);
    }
}
