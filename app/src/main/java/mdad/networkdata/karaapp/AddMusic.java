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

public class AddMusic extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid, is_staff, username, intent_from, attachUrl, attachName, attachArtist;
    private Boolean is_staffBoolean;
//    Declaration of String for music details.
    private String musicName, musicArtist, musicUrl, musicCreatedBy;
//    Declaration of components from AddMusic's xml
    private EditText inputMusic, inputArtist, inputUrl;
    private Button btnAttach, btnSubmit;
    private YouTubePlayer youTubePlayer;
    private boolean isFullscreen = false;
//    Declaration of Url address and requestType for postData
    private static String url_create_music = MainMenu.ipBaseAddress+"create_musicVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int create_music = 1, update_device = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_add_music);
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
        intent_from = intent.getStringExtra("intent_from_activity");

//        Get Variables from YoutubeAttach page
        attachUrl = intent.getStringExtra("attachUrl");
        attachName = intent.getStringExtra("attachName");
        attachArtist = intent.getStringExtra("attachArtist");

//        Declaration of EditText components tide to corresponding EditText id
        inputMusic = findViewById(R.id.inputMusic);
        inputArtist = findViewById(R.id.inputArtist);
        inputUrl = findViewById(R.id.inputUrl);

//        Initialize text into EditText from YoutubeAttach
        inputUrl.setText(attachUrl);
        inputMusic.setText(attachName);
        inputArtist.setText(attachArtist);

//        Button Attach to navigate to YoutubeAttach Page
        btnAttach = findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(AddMusic.this,YoutubeAttach.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("username", username);
                intent.putExtra("intent_from", "AddMusic");
                intent.putExtra("intent_from_activity", intent_from);
                startActivity(intent);
            }
        });

//        Button Submit to create music in database
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
//                Retrieve inputs from corresponding components
                musicName = inputMusic.getText().toString();
                musicArtist = inputArtist.getText().toString();
                musicUrl = inputUrl.getText().toString();
                musicCreatedBy = username;
//                Music Name and Music Url inputs are compulsory
                if (musicName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (musicUrl.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                }

//                Store inputs into Map parameter to perform postData
                Map<String,String> params_create = new HashMap<String,String>();
                params_create.put("music_name", musicName);
                params_create.put("artist_name", musicArtist);
                params_create.put("url", musicUrl);
                params_create.put("created_by", musicCreatedBy);
                if (intent_from.equals("Session")){
                    params_create.put("is_played","0");
                } else if (intent_from.equals("History")){
                    params_create.put("is_played","1");
                }
                postData(url_create_music, params_create, create_music);
            }
        });

//        --------Start of YoutubePlayer Logic---------
//        Customize back button callback to exit fullscreen
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
//        Declaration of each layers for Youtube Player layouts
        YouTubePlayerView youTubePlayerView = findViewById(R.id.addPageYoutubePlayer);              // Embed Youtube Player
        LinearLayout linearLayout = findViewById(R.id.addPageLinearLayout);                         // Linear Layout contains other components except Youtube Player
        FrameLayout fullscreenViewContainer = findViewById(R.id.addPageFullScreenViewContainer);    // Container for youtube fullscreen
        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()                 // Player settings internal from original youtube
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
                    Window window = AddMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(AddMusic.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = AddMusic.this.getSupportActionBar();
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
                    Window window = AddMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(AddMusic.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = AddMusic.this.getSupportActionBar();
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
                AddMusic.this.youTubePlayer = youTubePlayer;
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
//                        Response specified for create music response
                        if (requestType == create_music) {
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (response.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database",
                                        Toast.LENGTH_LONG).show();
//                                Upon Success, navigate back to Main Menu
                                finish();

                                if (intent_from.equals("Session")){
                                    Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("is_staff", is_staff);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                } else if (intent_from.equals("History")){
                                    Intent intent = new Intent(getApplicationContext(), History.class);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("is_staff", is_staff);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                }
                            }
                        }
//                        Response specified for updating device if user log out
                        if (requestType == update_device) {
                            if (response.equals("Error, false request.")) {
                                Toast.makeText(getApplicationContext(), "Error in verify device",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            finish();
                            startActivityIntent(Login.class);
                        }
                    }
//                    Error Listener if Volley failed to fetch data with database
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error in accessing database", Toast.LENGTH_LONG).show();
            }
        }) {
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

//    Option Menu Item select listener
    @Override
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
//                Start the activity for the selected menu item
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
        Intent intent = new Intent(AddMusic.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}