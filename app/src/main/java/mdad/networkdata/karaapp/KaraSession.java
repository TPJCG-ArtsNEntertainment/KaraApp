package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

public class KaraSession extends AppCompatActivity {
    Button btnAddMusic, btnEditMusic, btnPlayedMusic, btnRemoveMusic, btnPower;
    YouTubePlayer youTubePlayer;
    ListView listView;
    LinearLayout buttonLinearLayout;
    ImageView closedSessionImage;
    TextView closedSessionLabel;
    ArrayList<HashMap<String, String>> musicsList;
    private boolean isFullscreen = false, is_staffBoolean, power;
    private int selectedPosition = -1;
    private String selectedMusicId, selectedMusicName, selectedMusicArtist, selectedMusicUrl, uid, is_staff;
    private final int get_all_queue_music = 1, update_delete_music =2, get_power=3, update_power=4;
    // url to get all products list via the php file get_all_productsJson.php
    public static String ipBaseAddress = "http://aetpjcgkara.atspace.cc/";
    private static String url_all_queue_musics = KaraSession.ipBaseAddress+"get_all_musicQueueVolley.php";
    private static String url_update_musio = KaraSession.ipBaseAddress+"update_musicVolley.php";
    private static String url_delete_music = KaraSession.ipBaseAddress+"delete_musicVolley.php";
    private static String url_get_power = KaraSession.ipBaseAddress+"get_powerVolley.php";
    private static String url_update_power = KaraSession.ipBaseAddress+"update_powerVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kara_session);

//        YoutubePlayer Logic
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.mainPageYoutubePlayer);
        LinearLayout linearLayout = findViewById(R.id.mainPageLinearLayout);
        FrameLayout fullscreenViewContainer = findViewById(R.id.mainPageFullScreenViewContainer);
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
                    Window window = KaraSession.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(KaraSession.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) KaraSession.this).getSupportActionBar();
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
                    Window window = KaraSession.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(KaraSession.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) KaraSession.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            }
        });
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                KaraSession.this.youTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo("7L3Hdp86aio", 0f);
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        End of Logic

        // get resource id of ListView
        listView = (ListView)findViewById(R.id.listViewQueue);
        // ArrayList to store product info in Hashmap for ListView
        musicsList = new ArrayList<HashMap<String, String>>();
        // re-usable method to use Volley to retrieve products from database
        postData(url_all_queue_musics, null, get_all_queue_music);

        btnAddMusic = (Button) findViewById(R.id.btnQueueAdd);
        btnAddMusic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            //Create an Intent here to load the second activity
                Intent intent = new Intent(KaraSession.this, AddMusic.class);
                startActivity(intent);
            }
        });

//        Kara Session Power Logic

        postData(url_get_power,null, get_power);
        closedSessionImage = (ImageView) findViewById(R.id.closedSessionImage);
        closedSessionLabel = (TextView) findViewById(R.id.closedSessionLabel);
        btnPower = (Button) findViewById(R.id.btnPower);
        btnPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> param_update = new HashMap<String, String>();
                // Toggle the power state
                int newPowerState = power ? 0 : 1;
                param_update.put("power_state", String.valueOf(newPowerState));
                postData(url_update_power, param_update, update_power);
            }
        });

        btnEditMusic = (Button) findViewById(R.id.btnQueueEdit);
        btnEditMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(selectedMusicId);
                Intent intent = new Intent(getApplicationContext(), EditMusic.class);
                intent.putExtra("mid", selectedMusicId);
                startActivity(intent);
            }
        });

        btnPlayedMusic = (Button) findViewById(R.id.btnSetPlayed);
        btnPlayedMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid", selectedMusicId);
                params_update.put("music_name", selectedMusicName);
                params_update.put("artist_name", selectedMusicArtist);
                params_update.put("url", selectedMusicUrl);
                params_update.put("is_played", "1");
                postData(url_update_musio, params_update, update_delete_music);
            }
        });

        btnRemoveMusic = (Button) findViewById(R.id.btnQueueRemove);
        btnRemoveMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid", selectedMusicId);
                postData(url_delete_music, params_update, update_delete_music);
            }
        });
        buttonLinearLayout = findViewById(R.id.mainPageButtonLinearLayout);

//        Check Staff
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");
        if(!is_staffBoolean){
            buttonLinearLayout.setVisibility(View.GONE);
            btnPower.setVisibility(View.GONE);
        }else if(is_staffBoolean){
            buttonLinearLayout.setVisibility(View.VISIBLE);
            btnPower.setVisibility(View.VISIBLE);
        }
        if(selectedMusicId == null){
            btnEditMusic.setClickable(false);
            btnEditMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnPlayedMusic.setClickable(false);
            btnPlayedMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnRemoveMusic.setClickable(false);
            btnRemoveMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    public void postData(String url, Map params, final int requestType) {
        Context context = this;
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
                                            KaraSession.this, musicsList,
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
                                                selectedMusicId = rowData.get("music_id");
                                                selectedMusicName = rowData.get("music_name");
                                                selectedMusicArtist = rowData.get("artist_name");
                                                selectedMusicUrl = rowData.get("url");
                                                String videoId = extractVideoId(selectedMusicUrl);
                                                youTubePlayer.loadVideo(videoId,1);
                                                Toast.makeText(getApplicationContext(), "Playing: " + selectedMusicName, Toast.LENGTH_SHORT).show();

                                                buttonLinearLayout.setVisibility(View.VISIBLE);

                                                if(is_staffBoolean) {
                                                    btnEditMusic.setClickable(true);
                                                    btnEditMusic.setBackgroundColor(0xFF6200EE);
                                                    btnPlayedMusic.setClickable(true);
                                                    btnPlayedMusic.setBackgroundColor(0xFF6200EE);
                                                    btnRemoveMusic.setClickable(true);
                                                    btnRemoveMusic.setBackgroundColor(0xFF6200EE);
                                                }

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
                            listView.setAdapter(adapter);
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
                                    if (musicId.equals(selectedMusicId)) {
                                        musicsList.remove(i);
                                        break;
                                    }

                                }
                                selectedPosition = -1;
                                btnEditMusic.setClickable(false);
                                btnEditMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                btnPlayedMusic.setClickable(false);
                                btnPlayedMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                btnRemoveMusic.setClickable(false);
                                btnRemoveMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                            }
                        }
                        if (requestType == get_power){
                            if (response.equals(" Error")) {
                                Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            }
                            power = response.equals("1");
                            if (!power){
                                closedSessionImage.setVisibility(View.GONE);
                                closedSessionLabel.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                                btnAddMusic.setClickable(true);
//                                btnAddMusic.setBackgroundColor(0xFF6200EE);
                                btnAddMusic.setBackgroundColor(0xFF6200EE);
                                btnPower.setText("Off");
                                btnPower.setBackgroundColor(0xFFFF0000);
                            }
                            if (power) {
                                listView.setVisibility(View.GONE);
                                closedSessionImage.setVisibility(View.VISIBLE);
                                closedSessionLabel.setVisibility(View.VISIBLE);
                                btnAddMusic.setClickable(false);
                                btnAddMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                btnPower.setText("On");
                                btnPower.setBackgroundColor(0xFF4CAF50);
                            }
                        }
                        if (requestType == update_power){
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
                                postData(url_get_power,null,get_power);
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
                youTubePlayer.toggleFullscreen();
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
        Intent intent = new Intent(KaraSession.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        startActivity(intent);
    }
}