package mdad.networkdata.karaapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
    private String uid, is_staff, username;
    private Boolean is_staffBoolean;
    private EditText inputMusic,inputArtist,inputUrl;
    private String attachUrl,attachName,attachArtist,musicName,musicArtist,musicUrl,musicCreatedBy;
    private Button btnAttach,btnSubmit;
    private YouTubePlayer youTubePlayer; // Declare youTubePlayer here
    private boolean isFullscreen = false;
    private static String url_create_music = MainMenu.ipBaseAddress+"create_musicVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int create_music=1, update_device=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        attachUrl = intent.getStringExtra("attachUrl");
        attachName = intent.getStringExtra("attachName");
        attachArtist = intent.getStringExtra("attachArtist");

        inputMusic = (EditText) findViewById(R.id.inputMusic);
        inputArtist = (EditText) findViewById(R.id.inputArtist);
        inputUrl = (EditText) findViewById(R.id.inputUrl);

        inputUrl.setText(attachUrl);
        inputMusic.setText(attachName);
        inputArtist.setText(attachArtist);

//        YoutubePlayer Logic
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.addPageYoutubePlayer);
        LinearLayout linearLayout = findViewById(R.id.addPageLinearLayout);
        FrameLayout fullscreenViewContainer = findViewById(R.id.addPageFullScreenViewContainer);
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
                    Window window = AddMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(AddMusic.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) AddMusic.this).getSupportActionBar();
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
                    Window window = AddMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(AddMusic.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) AddMusic.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            }
        });
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                AddMusic.this.youTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo("7L3Hdp86aio", 0f);
                if (attachUrl != null) {
                    String videoId = extractVideoId(attachUrl);
                    youTubePlayer.loadVideo(videoId,1);
                }
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        End of Logic

        btnAttach = (Button) findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddMusic.this,YoutubeAttach.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                musicName = inputMusic.getText().toString();
                musicArtist = inputArtist.getText().toString();
                musicUrl = inputUrl.getText().toString();
                musicCreatedBy = MainMenu.username;
                if (musicName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (musicUrl.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Map<String,String> params_create = new HashMap<String,String>();
                params_create.put("music_name", musicName);
                params_create.put("artist_name", musicArtist);
                params_create.put("url", musicUrl);
                params_create.put("created_by", musicCreatedBy);

                postData(url_create_music, params_create, create_music);
            }
        });
    }
    public void postData(String url, Map params, final int requestType) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (requestType == create_music){
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                            }
                            if (response.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database",
                                        Toast.LENGTH_LONG).show();
                                finish();
                                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                intent.putExtra("username", username);
                                startActivity(intent);
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
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error in accessing database",Toast.LENGTH_LONG).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() { return params; }
        };
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
            menu.findItem(R.id.item3).setVisible(false);
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
        Intent intent = new Intent(AddMusic.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}