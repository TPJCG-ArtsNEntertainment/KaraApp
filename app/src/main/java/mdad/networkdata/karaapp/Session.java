package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
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

public class Session extends Fragment {
//    Initialization parameters for fragment
    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
    private String mParam1, mParam2;
//    Empty public constructor for fragment
    public Session(){};
//    Create new instance of fragment with this method with parameters
    public static Session newInstance(String param1, String param2) {
        Session session = new Session();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        session.setArguments(args);
        return session;
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
        return inflater.inflate(R.layout.activity_session, container, false);
    }

//    ------ Beginning of fragment customization ------
//    Declaration of String for selected item and Boolean for fullscreen and power status
    private String selectedMusicId, selectedMusicName, selectedMusicArtist, selectedMusicUrl, selectedMusicCreatedBy;
    private boolean isFullscreen = false, power;
//    Declaration of components from Session's xml
    private Button btnAddMusic, btnPower;
    private YouTubePlayer youTubePlayer;
    private ListView listView;
    private SearchView queueSearchView;
    private ImageView closedSessionImage;
    private TextView closedSessionLabel;
//    Declaration of Array for music list and filter
    private ArrayList<HashMap<String, String>> musicsList, originalMusicsList,filteredMusicsList,targetList;
//    Declaration of Url address for postData
    private static String url_all_queue_musics = MainMenu.ipBaseAddress+"get_all_musicQueueVolley.php";
    private static String url_update_music = MainMenu.ipBaseAddress+"update_musicVolley.php";
    private static String url_delete_music = MainMenu.ipBaseAddress+"delete_musicVolley.php";
    private static String url_get_power = MainMenu.ipBaseAddress+"get_powerVolley.php";
    private static String url_update_power = MainMenu.ipBaseAddress+"update_powerVolley.php";
    private final int get_all_queue_music = 1, update_delete_music =2, get_power=3, update_power=4;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

//         Declaration of ListView
        listView = view.findViewById(R.id.listViewQueue);
        registerForContextMenu(listView);
//         ArrayList to store music info in Hashmap for ListView
        musicsList = new ArrayList<HashMap<String, String>>();
//         Perform postData to update ListView
        postData(url_all_queue_musics, null, get_all_queue_music);

//        Get original ListAdapter from ListView's musicHistoryList
        ListAdapter originalAdapter = new SimpleAdapter(
                requireActivity(), musicsList,
                R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"},
                new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
        );
        originalMusicsList = musicsList;
        filteredMusicsList = new ArrayList<>(originalMusicsList);
        listView.setAdapter(originalAdapter);

//        Declaration of Search bar, query tide to update ListView with filter
        queueSearchView = view.findViewById(R.id.queueSearchView);
        queueSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

//        Button Add Music to navigate to AddMusic Page
        btnAddMusic = view.findViewById(R.id.btnQueueAdd);
        btnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //Create an Intent here to load the second activity
                Intent intent = new Intent(requireActivity(), AddMusic.class);
                intent.putExtra("uid", MainMenu.uid);
                intent.putExtra("is_staff", MainMenu.is_staff);
                intent.putExtra("username", MainMenu.username);
                intent.putExtra("intent_from_activity", "Session");
                startActivity(intent);
            }
        });

//      -------- Power Logic (General purpose to stop participant add music into queue when event end)-------
//        Retrieve power status (Database control)
        postData(url_get_power,null, get_power);
//        Declaration of display for closed session and power button
        closedSessionImage = view.findViewById(R.id.closedSessionImage);
        closedSessionLabel = view.findViewById(R.id.closedSessionLabel);
        btnPower = view.findViewById(R.id.btnPower);
        btnPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> param_update = new HashMap<String, String>();
//                 Toggle the power state
                int newPowerState = power ? 0 : 1;
                param_update.put("power_state", String.valueOf(newPowerState));
                postData(url_update_power, param_update, update_power);
            }
        });
//        Non staff user should not be able to toggle power button
        if(!MainMenu.is_staffBoolean){
            btnPower.setVisibility(View.GONE);
        }
//        End of Power Logic

//        --------Start of YoutubePlayer Logic---------
//        Customize back button callback to exit fullscreen
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), onBackPressedCallback);
//        Declaration of each layers for Youtube Player layouts
        YouTubePlayerView youTubePlayerView = view.findViewById(R.id.mainPageYoutubePlayer);
        LinearLayout linearLayout = view.findViewById(R.id.mainPageLinearLayout);
        FrameLayout fullscreenViewContainer = view.findViewById(R.id.mainPageFullScreenViewContainer);
        TabLayout tabLayout = requireActivity().findViewById(R.id.tab_layout);
        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .build();
//        Disable auto initialization as we will customize initialization
        youTubePlayerView.setEnableAutomaticInitialization(false);
//        Youtube player fullscreen listener
        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
//            -------- On Entering Fulllscreen----------
            @Override
            public void onEnterFullscreen(View fullscreenView, Function0<Unit> exitFullscreen) {
//                Hide components upon Fullscreen and unhide fullscreen container
                isFullscreen = true;
                youTubePlayerView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                tabLayout.setVisibility(View.GONE);
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);
//                Hide Action Bar upon fullscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = requireActivity().getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(Session.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                    }
                }
//                Hide Notification bar on Android
                View decorView = requireActivity().getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
//            -------------- On Exit Fullscreen --------------
            @Override
            public void onExitFullscreen() {
//                Unhide components upon Fullscreen and hide fullscreen container
                isFullscreen = false;
                youTubePlayerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
//                Show Action Bar upon fullscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = requireActivity().getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(Session.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
//                Show Notification bar on Android
                View decorView = requireActivity().getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
            }
        });
//        Customize initialization of Youtube Player
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                Session.this.youTubePlayer = youTubePlayer;
                if (musicsList.size() > 0) {
                    HashMap<String, String> firstVideo = musicsList.get(0);
                    String firstVideoUrl = firstVideo.get("url");
                    String firstVideoId = extractVideoId(firstVideoUrl);
                    youTubePlayer.cueVideo(firstVideoId, 0f);
                }
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        ---------End of Youtube Player Logic--------
    }

//    --------- Dynamic function to extract Video Id from Youtube URL ---------
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

//    ---------- Function to customize Android back button upon fullscreen ----------
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (isFullscreen) {
                youTubePlayer.toggleFullscreen();
            }
        }
    };


//    --------------- General Functions for context menu navigation for ListView item --------------
//    Create Context Menu in ListView Item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        Inflate the context menu and display context menu when item long pressed
        requireActivity().getMenuInflater().inflate(R.menu.menu_music, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        Retrieve selected item data into HashMap
        HashMap<String, String> rowData = musicsList.get(info.position);
        Boolean is_owner = MainMenu.username.equals(rowData.get("created_by"));
        menu.findItem(R.id.option_set_unplayed).setVisible(false);
//        Privilege for staff to control music list
        if (!MainMenu.is_staffBoolean) {
            menu.findItem(R.id.option_set_played).setVisible(false);
            menu.findItem(R.id.option_edit).setVisible(false);
            menu.findItem(R.id.option_remove).setVisible(false);
        }
//        Privilege for item created owner (Only apply if created_by is same as logged in user)
        if (is_owner){
            menu.findItem(R.id.option_edit).setVisible(true);
            menu.findItem(R.id.option_remove).setVisible(true);
        }
    }

//    Context Menu Item select listener
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
//        Retrieve information from selected item
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        Set targetList based on filter status
        if (filteredMusicsList.isEmpty()) targetList = musicsList;
        else targetList = filteredMusicsList;
//        Retrieve selected item data into HashMap and variables
        HashMap<String, String> rowData = targetList.get(info.position);
        selectedMusicId = rowData.get("music_id");
        selectedMusicName = rowData.get("music_name");
        selectedMusicArtist = rowData.get("artist_name");
        selectedMusicUrl = rowData.get("url");
        selectedMusicCreatedBy = rowData.get("created_by");
//        Handling each contextItem option selection
        int itemId = item.getItemId();
        if (itemId == R.id.option_play) {
            String videoId = extractVideoId(selectedMusicUrl);
            youTubePlayer.loadVideo(videoId,1);
            Toast.makeText(requireActivity(), "Playing: " + selectedMusicName, Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.option_set_played) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicId);
            params_update.put("music_name", selectedMusicName);
            params_update.put("artist_name", selectedMusicArtist);
            params_update.put("url", selectedMusicUrl);
            params_update.put("created_by", selectedMusicCreatedBy);
            params_update.put("is_played", "1");
            postData(url_update_music, params_update, update_delete_music);
        } else if (itemId == R.id.option_set_unplayed) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicId);
            params_update.put("music_name", selectedMusicName);
            params_update.put("artist_name", selectedMusicArtist);
            params_update.put("url", selectedMusicUrl);
            params_update.put("created_by", selectedMusicCreatedBy);
            params_update.put("is_played", "0");
            postData(url_update_music, params_update, update_delete_music);
        } else if (itemId == R.id.option_edit) {
            Intent intent = new Intent(requireActivity(), EditMusic.class);
            intent.putExtra("uid", MainMenu.uid);
            intent.putExtra("is_staff", MainMenu.is_staff);
            intent.putExtra("username", MainMenu.username);
            intent.putExtra("mid", selectedMusicId);
            intent.putExtra("intent_from", "MusicListView");
            startActivity(intent);
        } else if (itemId == R.id.option_remove) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicId);
            postData(url_delete_music, params_update, update_delete_music);
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

//  ------------- General function apply for Search bar to the corresponding List----------
    private void filter(String query) {
//        Initialize filter list
        filteredMusicsList.clear();
        if (TextUtils.isEmpty(query)) {
//            If the query is empty, show all items
            filteredMusicsList.addAll(originalMusicsList);
        } else {
//            Filter items based on the query
            for (HashMap<String, String> music : originalMusicsList) {
                if (music.get("music_name").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicsList.add(music);
                } else if (music.get("artist_name").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicsList.add(music);
                } else if (music.get("created_at").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicsList.add(music);
                } else if (music.get("created_by").toLowerCase().contains(query.toLowerCase())) {
                    filteredMusicsList.add(music);
                }
            }
        }
//        Update the adapter with the filtered data
        ListAdapter filteredAdapter = new SimpleAdapter(
                requireActivity(), filteredMusicsList,
                R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"},
                new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
        ){
//            Using Picasso to display preview image with youtube video ID
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final HashMap<String, String> rowData = filteredMusicsList.get(position);
                ImageView youtubePreviewImage = view.findViewById(R.id.youtubePreviewImage);
                String videoId = extractVideoId(rowData.get("url"));
                String imageUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Picasso.get().load(imageUrl).into(youtubePreviewImage);
                return view;
            }
        };
        listView.setAdapter(filteredAdapter);
    }

//    ---------- PostData Volley Function ----------
    public void postData(String url, Map params, final int requestType) {
//        Declaration of volley request
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
//        Declaration of string request for post parameters
        StringRequest stringRequest = new StringRequest( Request.Method.POST, url,
//                Upon receiving response, actions to be done.
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Response specified for retrieve all music queue response
                        if (requestType == get_all_queue_music) {
//                            check if error code received from server.
                            if (response.equals("Error")) {
                                Toast.makeText(requireActivity(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Clear musicList
                            musicsList.clear();
//                            store each music from database in String array
                            String[] musics = response.split("\\|");
//                             for each music, retrieve the music details
                            for (int i = 0; i < musics.length; i++) {
//                                 Storing each music info in variable
                                String[] details = musics[i].split(";");
                                String mid = details[0];
                                String musicName = details[1];
                                String artistName = details[2];
                                String url = details[3];
                                String created_at = details[4];
                                String created_by = details[5];

                                // HashMap for each music details
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("music_id", mid);
                                map.put("music_name", musicName);
                                map.put("artist_name", artistName);
                                map.put("url", url);
                                map.put("created_at", created_at);
                                map.put("created_by", created_by);

                                // adding map HashList to ArrayList
                                musicsList.add(map);
                            }
//                            populate the listview with music information from Hashmap
                            ListAdapter adapter = new SimpleAdapter(
                                            requireActivity(), musicsList,
                                            R.layout.list_view_musics, new String[]{"music_id", "music_name", "url", "artist_name", "created_at", "created_by"}, new int[]{R.id.mid, R.id.mName, R.id.mUrl, R.id.mArtist, R.id.mCreatedBy, R.id.mCreatedAt}
                                    ){
//                                      Using Picasso to display preview image with youtube video ID
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        final HashMap<String, String> rowData = musicsList.get(position);
                                        ImageView youtubePreviewImage = view.findViewById(R.id.youtubePreviewImage);
                                        String videoId = extractVideoId(rowData.get("url"));
                                        String imageUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                                        Picasso.get().load(imageUrl).into(youtubePreviewImage);
                                        return view;
                                    }
                                };
//                             Updating listview
                            listView.setAdapter(adapter);
                        }
//                        Response specified for updating and deleting music from music history
                        if (requestType == update_delete_music) {
                            if (response.trim().equals("Error")) {
                                Toast.makeText(requireActivity(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.trim().equals("Success")) {
                                Toast.makeText(requireActivity(), "Success in updating database", Toast.LENGTH_LONG).show();
                                for (int i = 0; i < musicsList.size(); i++) {
                                    HashMap<String, String> map = musicsList.get(i);
                                    String musicId = map.get("music_id");
                                    if (musicId.equals(selectedMusicId)) {
                                        musicsList.remove(i);
                                        break;
                                    }

                                }
//                                Refresh music queue list
                                postData(url_all_queue_musics, null, get_all_queue_music);
                                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                            }
                        }
//                        Response specified for retrieving power status
                        if (requestType == get_power){
//                            Handle error in php
                            if (response.equals(" Error")) {
                                Toast.makeText(requireActivity(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            }
//                            Handle boolean power status
                            power = response.equals("1"); //convert response from string to boolean
                            if (!power){
//                                Hide closed session display and show listview
                                closedSessionImage.setVisibility(View.GONE);
                                closedSessionLabel.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
//                                Set Button click availability and color
                                btnAddMusic.setClickable(true);
//                                Using TypedValue to retrieve hex code of the color theme from theme.xml
                                TypedValue typedValue = new TypedValue();
                                requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                                int colorPrimary = ContextCompat.getColor(requireActivity(), typedValue.resourceId);
                                btnAddMusic.setBackgroundColor(colorPrimary);
                                btnPower.setBackgroundColor(0xFFFF0000);
                            }
                            if (power) {
//                                Show closed session display and hide listview
                                listView.setVisibility(View.GONE);
                                closedSessionImage.setVisibility(View.VISIBLE);
                                closedSessionLabel.setVisibility(View.VISIBLE);
//                                Set Button click availability and color
                                btnAddMusic.setClickable(false);
                                btnAddMusic.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                btnPower.setBackgroundColor(0xFF4CAF50);
                            }
                        }
//                        Response specified for updating power status
                        if (requestType == update_power){
//                            Handling error from php
                            if (response.equals("Error")) {
                                Toast.makeText(requireActivity(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
//                            Handling success updating power status
                            if (response.equals("Success")) {
                                Toast.makeText(requireActivity(), "Success in updating database", Toast.LENGTH_LONG).show();
//                                Refresh power status
                                postData(url_get_power,null,get_power);
                            }
                        }
                    }
                },
//                    Error Listener if Volley failed to fetch data with database
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    // handle error
                        Toast.makeText(requireActivity(),"Error in retrieving database",Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
//        perform Volley Request
        requestQueue.add(stringRequest);
    }
}