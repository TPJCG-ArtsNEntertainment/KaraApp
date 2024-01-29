package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class History extends AppCompatActivity {
    public static String uid, is_staff, username;
    public static Boolean is_staffBoolean;
    private static String url_all_history_musics = MainMenu.ipBaseAddress+"get_all_musicHistoryVolley.php";
    private static String url_update_music = MainMenu.ipBaseAddress+"update_musicVolley.php";
    private static String url_delete_music = MainMenu.ipBaseAddress+"delete_musicVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private Button btnAddMusic;
    private YouTubePlayer youTubePlayerHistory;
    private ListView recyclerViewHistory;
    private SearchView historySearchView;
    private boolean isFullscreen = false;
    private String selectedMusicIdHistory, selectedMusicNameHistory, selectedMusicArtistHistory, selectedMusicUrlHistory, selectedMusicCreatedByHistory;
    private final int get_all_history_music = 1, update_delete_music =2, update_device=3;
    private ArrayList<HashMap<String, String>> musicHistoryList, originalMusicHistoryList, filteredMusicHistoryList, targetHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        // get resource id of ListView
        recyclerViewHistory = (ListView) findViewById(R.id.listViewHistory);
        registerForContextMenu(recyclerViewHistory);
        // ArrayList to store product info in Hashmap for ListView
        musicHistoryList = new ArrayList<HashMap<String, String>>();
        // re-usable method to use Volley to retrieve products from database
        postData(url_all_history_musics, null, get_all_history_music);

        historySearchView = findViewById(R.id.historySearchView);
        ListAdapter originalAdapter = new SimpleAdapter(
                getApplicationContext(), musicHistoryList,
                R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"},
                new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
        );
        originalMusicHistoryList = musicHistoryList;
        filteredMusicHistoryList = new ArrayList<>(originalMusicHistoryList);
        recyclerViewHistory.setAdapter(originalAdapter);
        historySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the items based on the search query
                filter(newText);
                return true;
            }
        });

        btnAddMusic = (Button) findViewById(R.id.btnHistoryAdd);
        btnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create an Intent here to load the second activity
                Intent intent = new Intent(getApplicationContext(), AddMusic.class);
                intent.putExtra("uid", MainMenu.uid);
                intent.putExtra("is_staff", MainMenu.is_staff);
                intent.putExtra("username", MainMenu.username);
                startActivity(intent);
            }
        });

//        YoutubePlayer Logic
        getOnBackPressedDispatcher().addCallback(History.this, onBackPressedCallback);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.historyPageYoutubePlayer);
        LinearLayout linearLayout = findViewById(R.id.historyPageLinearLayout);
        FrameLayout fullscreenViewContainer = findViewById(R.id.historyPageFullScreenViewContainer);
//        TabLayout tabLayout = getApplicationContext().findViewById(R.id.tab_layout);
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
//                tabLayout.setVisibility(View.GONE);
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = History.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(History.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) getApplicationContext()).getSupportActionBar();
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
//                tabLayout.setVisibility(View.VISIBLE);
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = History.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(History.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) getApplicationContext()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            }
        });
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayerHistory) {
                History.this.youTubePlayerHistory = youTubePlayerHistory;
                if (musicHistoryList.size() > 0) {
                    HashMap<String, String> firstVideo = musicHistoryList.get(0);
                    String firstVideoUrl = firstVideo.get("url");
                    String firstVideoId = extractVideoId(firstVideoUrl);
                    youTubePlayerHistory.cueVideo(firstVideoId, 0f);
                }
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        End of Youtube Player Logic
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.getMenuInflater().inflate(R.menu.menu_music, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        HashMap<String, String> rowData = musicHistoryList.get(info.position);
        System.out.println(rowData);
        Boolean is_owner = MainMenu.username.equals(rowData.get("created_by"));
        menu.findItem(R.id.option_set_played).setVisible(false);

        if (!MainMenu.is_staffBoolean) {
            menu.findItem(R.id.option_set_unplayed).setVisible(false);
            menu.findItem(R.id.option_edit).setVisible(false);
            menu.findItem(R.id.option_remove).setVisible(false);
        }
        if (is_owner){
            menu.findItem(R.id.option_edit).setVisible(true);
            menu.findItem(R.id.option_remove).setVisible(true);
        }
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.d("History", "onContextItemSelected in History.java");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (filteredMusicHistoryList.isEmpty()) targetHistoryList = musicHistoryList;
        else targetHistoryList = filteredMusicHistoryList;
        HashMap<String, String> rowDataHistory = targetHistoryList.get(info.position);
        selectedMusicIdHistory = rowDataHistory.get("mid");
        selectedMusicNameHistory = rowDataHistory.get("music_name");
        selectedMusicArtistHistory = rowDataHistory.get("artist_name");
        selectedMusicUrlHistory = rowDataHistory.get("url");
        selectedMusicCreatedByHistory = rowDataHistory.get("created_by");
        int itemId = item.getItemId();
        if (itemId == R.id.option_play) {
            String videoId = extractVideoId(selectedMusicUrlHistory);
            youTubePlayerHistory.loadVideo(videoId,1);
            Toast.makeText(getApplicationContext(), "Playing: " + selectedMusicNameHistory, Toast.LENGTH_SHORT).show();
//        } else if (itemId == R.id.option_download_video) {
//        } else if (itemId == R.id.option_download_music) {
        } else if (itemId == R.id.option_set_played) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicIdHistory);
            params_update.put("music_name", selectedMusicNameHistory);
            params_update.put("artist_name", selectedMusicArtistHistory);
            params_update.put("url", selectedMusicUrlHistory);
            params_update.put("created_by", selectedMusicCreatedByHistory);
            params_update.put("is_played", "1");
            postData(url_update_music, params_update, update_delete_music);
        } else if (itemId == R.id.option_set_unplayed) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicIdHistory);
            params_update.put("music_name", selectedMusicNameHistory);
            params_update.put("artist_name", selectedMusicArtistHistory);
            params_update.put("url", selectedMusicUrlHistory);
            params_update.put("created_by", selectedMusicCreatedByHistory);
            params_update.put("is_played", "0");
            postData(url_update_music, params_update, update_delete_music);
        } else if (itemId == R.id.option_edit) {
            Intent intent = new Intent(getApplicationContext(), EditMusic.class);
            intent.putExtra("uid", MainMenu.uid);
            intent.putExtra("is_staff", MainMenu.is_staff);
            intent.putExtra("username", MainMenu.username);
            intent.putExtra("mid", selectedMusicIdHistory);
            startActivity(intent);
        } else if (itemId == R.id.option_remove) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicIdHistory);
            postData(url_delete_music, params_update, update_delete_music);
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

    private void filter(String query) {
        filteredMusicHistoryList.clear();
        if (TextUtils.isEmpty(query)) {
            // If the query is empty, show all items
            filteredMusicHistoryList.addAll(originalMusicHistoryList);
        } else {
            // Filter items based on the query
            for (HashMap<String, String> music : originalMusicHistoryList) {
                if (music.get("music_name").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicHistoryList.add(music);
                } else if (music.get("artist_name").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicHistoryList.add(music);
                } else if (music.get("created_at").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicHistoryList.add(music);
                } else if (music.get("created_by").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicHistoryList.add(music);
                }
            }
        }
        // Reverse the order of the list
//        Collections.reverse(filteredMusicsList);
        // Update the adapter with the filtered data
        ListAdapter filteredAdapter = new SimpleAdapter(
                getApplicationContext(), filteredMusicHistoryList,
                R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"},
                new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
        ){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final HashMap<String, String> rowData = filteredMusicHistoryList.get(position);
                ImageView youtubePreviewImage = view.findViewById(R.id.youtubePreviewImage);
                String videoId = extractVideoId(rowData.get("url"));
                String imageUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Picasso.get().load(imageUrl).into(youtubePreviewImage);
                return view;
            }
        };
        recyclerViewHistory.setAdapter(filteredAdapter);
    }

    public void postData(String url, Map params, final int requestType) {
        //create a RequestQueue for Volley
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //create a StringRequest for Volley for HTTP Post
        StringRequest stringRequest = new StringRequest( Request.Method.POST, url,
                //response from server
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (requestType == get_all_history_music) {
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
                                musicHistoryList.add(map);
                            }
                            Collections.reverse(musicHistoryList);
                            //populate the listview with product information from Hashmap
                            ListAdapter adapter = new SimpleAdapter(
                                    getApplicationContext(), musicHistoryList,
                                    R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"}, new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
                            ){
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    final HashMap<String, String> rowData = musicHistoryList.get(position);
                                    ImageView youtubePreviewImage = view.findViewById(R.id.youtubePreviewImage);
                                    String videoId = extractVideoId(rowData.get("url"));
                                    String imageUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                                    Picasso.get().load(imageUrl).into(youtubePreviewImage);
                                    return view;
                                }
                            };
                            // updating listview
                            recyclerViewHistory.setAdapter(adapter);
                        }
                        if (requestType == update_delete_music) {
                            System.out.println(response);
                            if (response.trim().equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.trim().equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
                                for (int i = 0; i < musicHistoryList.size(); i++) {
                                    HashMap<String, String> map = musicHistoryList.get(i);
                                    String musicId = map.get("music_id");
                                    if (musicId.equals(selectedMusicIdHistory)) {
                                        musicHistoryList.remove(i);
                                        break;
                                    }

                                }
                                ((BaseAdapter) recyclerViewHistory.getAdapter()).notifyDataSetChanged();
                            }
                        }
                        if (requestType == update_device){
                            if (response.equals("Error, false request.")) {
                                Toast.makeText(getApplicationContext(), "Error in verify device",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            finish();
                            startActivityIntent(Login.class);
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
                //send mid stored in HashMap using HTTP Post in Volley
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
                youTubePlayerHistory.toggleFullscreen();
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
            menu.findItem(R.id.item2).setVisible(false);
        }
        return true;
    }
    @Override
    //when the option item is selected
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        Class<?>[] destinationClasses = {MainMenu.class, History.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class};
        // Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
            if (id == R.id.item6){
                Map<String, String> param_update = new HashMap<>();
                param_update.put("uid", uid);
                param_update.put("token", "");
                postData(url_update_device, param_update, update_device);
            } else if (id == menuItems[i]) {
                // Start the activity for the selected menu item
                finish();
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
        Intent intent = new Intent(History.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}