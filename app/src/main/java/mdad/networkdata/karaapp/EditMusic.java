package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class EditMusic extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid, is_staff, username, mid, intent_from, attachUrl, attachName, attachArtist;
    private Boolean is_staffBoolean;
//    Declaration of String for updating music details.
    private String music_name, music_artist, music_url, is_played;
//    Declaration of components from EditMusic's xml
    private EditText editTextMusic, editTextArtist, editTextUrl;
    private Button btnEditAttach, btnEditSubmit;
    private YouTubePlayer youTubePlayer;
    private boolean isFullscreen = false;
//    Declaration of Url address and requestType for postData
    private static final String url_music_details = MainMenu.ipBaseAddress+"get_music_detailsVolley.php";
    private static final String url_update_product = MainMenu.ipBaseAddress+"update_musicVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int get_music_details = 1, update_music = 2, update_device = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_edit_music);
//        Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Get Variables from previous page
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

//        Get Music variables from either MainMenu's Session/ History/ YoutubeAttach page
        mid = intent.getStringExtra("mid");
        is_played = intent.getStringExtra("is_played");
        intent_from = intent.getStringExtra("intent_from");
//        Only perform reading music detail from database when navigate from MainMenu's Session / History
        if (intent_from.equals("MusicListView")){
            Map<String,String> param_mid = new HashMap<String, String>();
            param_mid.put("mid",mid);
            postData(url_music_details, param_mid, get_music_details);
        }
//        Get Variables from YoutubeAttach page
        attachUrl = intent.getStringExtra("attachUrl");
        attachName = intent.getStringExtra("attachName");
        attachArtist = intent.getStringExtra("attachArtist");

//        Declaration of EditText components tide to corresponding EditText id
        editTextMusic = findViewById(R.id.inputEditMusic);
        editTextArtist = findViewById(R.id.inputEditArtist);
        editTextUrl = findViewById(R.id.inputEditUrl);

//        Initialize text into EditText from YoutubeAttach
        editTextMusic.setText(attachName);
        editTextArtist.setText(attachArtist);
        editTextUrl.setText(attachUrl);

//        Button Attach to navigate to YoutubeAttach Page
        btnEditAttach = findViewById(R.id.btnEditAttach);
        btnEditAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(EditMusic.this,YoutubeAttach.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("username", username);
                intent.putExtra("mid",mid);
                intent.putExtra("is_played", is_played);
                intent.putExtra("intent_from", "EditMusic");
                startActivity(intent);
            }
        });

//        Button Submit to update music in database
        btnEditSubmit = findViewById(R.id.btnEditSubmit);
        btnEditSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Retrieve inputs from corresponding components
                music_name=editTextMusic.getText().toString();
                music_artist=editTextArtist.getText().toString();
                music_url=editTextUrl.getText().toString();

//                Store inputs into Map parameter to perform postData
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid",mid);
                params_update.put("music_name",music_name);
                params_update.put("artist_name", music_artist);
                params_update.put("url", music_url);
                params_update.put("is_played", is_played);

                postData(url_update_product,params_update,update_music);
            }
        });

//        --------Start of YoutubePlayer Logic---------
//        Customize back button callback to exit fullscreen
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
//        Declaration of each layers for Youtube Player layouts
        LinearLayout linearLayout = findViewById(R.id.editPageLinearLayout);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.editPageYoutubePlayer);
        FrameLayout fullscreenViewContainer = findViewById(R.id.editPageFullScreenViewContainer);
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
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);
//                Hide Action Bar upon fullscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = EditMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(EditMusic.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) EditMusic.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                    }
                }
//                Hide Notification bar on Android
                View decorView = getWindow().getDecorView();
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
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
//                Show Action Bar upon fullscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Window window = EditMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(EditMusic.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) EditMusic.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
//                Show Notification bar on Android
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
            }
        });
//        Customize initialization of Youtube Player
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                EditMusic.this.youTubePlayer = youTubePlayer;
                youTubePlayer.cueVideo(extractVideoId(music_url), 0f);
//                Display attached youtube video based on URL link if available
                if (attachUrl != null) {
                    String videoId = extractVideoId(attachUrl);
                    youTubePlayer.cueVideo(videoId,1);
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
            } else {
                finish();
            }
        }
    };

//    ---------- PostData Volley Function ----------
    public void postData(String url, Map params, final int requestType) {
//        Declaration of volley request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        Declaration of string request for post parameters
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                Upon receiving response, actions to be done.
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Response specified for retrieve music detail response
                        if (requestType == get_music_details) {
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in accessing database", Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Split each response into array
                            String[] music = response.split("\\|");
                            String[] details = music[0].split(";");
                            mid = details[0];
                            music_name = details[1];
                            music_artist = details[2];
                            music_url = details[3];
                            is_played = details[6];
//                            Set the result into the component
                            editTextMusic.setText(music_name);
                            editTextArtist.setText(music_artist);
                            editTextUrl.setText(music_url);
                        }
//                        Response specified for update music response
                        if (requestType == update_music) {
                            if (response.trim().equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.trim().equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
//                                Upon Success, navigate back to User Management
                                finish();
                                Intent intent = new Intent(getApplicationContext(), Session.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                intent.putExtra("username", username);
                                startActivity(intent);
                            }
                        }
//                        Response specified for updating device if user log out
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
//                    Error Listener if Volley failed to fetch data with database
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error in Volley ", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
//        perform Volley Request
        requestQueue.add(stringRequest);
    }

//   --------------- General Functions for option menu navigation for Action Bar --------------
//    Create Option Menu in Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//         Inflate the option menu and display the option items when clicked;
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
//        Non staff user should not be able navigate to user management
        if (!is_staffBoolean) {
            menu.findItem(R.id.item3).setVisible(false);
        }
        return true;
    }
    @Override
//    Option Menu Item select listener
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//         Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        Class<?>[] destinationClasses = {MainMenu.class, History.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class};
//         Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
//            item6 is Log out, therefore updating device is needed
            if (id == R.id.item6){
                Map<String, String> param_update = new HashMap<>();
                param_update.put("uid", uid);
                param_update.put("token", "");
                postData(url_update_device, param_update, update_device);
//                Dynamic handling for other menu items
            } else if (id == menuItems[i]) {
//                 Start the activity for the selected menu item
                startActivityIntent(destinationClasses[i]);
                return true;
            } else if (id == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
//         If the selected item is not found in the loop, fallback to super.onOptionsItemSelected
        return super.onOptionsItemSelected(item);
    }

//    Dynamic function for intent
    private void startActivityIntent(Class<?> cls) {
        Intent intent = new Intent(EditMusic.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}