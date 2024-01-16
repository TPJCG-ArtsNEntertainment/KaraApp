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
    EditText inputMusic,inputArtist,inputUrl;
    String musicName,musicArtist,musicUrl,uid,is_staff;
    Boolean is_staffBoolean;
    Button btnAttach,btnSubmit;
    YouTubePlayer youTubePlayer; // Declare youTubePlayer here
    private boolean isFullscreen = false;
    private static String url_create_music = KaraSession.ipBaseAddress+"create_musicVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");

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
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        End of Logic

        inputMusic = (EditText) findViewById(R.id.inputMusic);
        inputArtist = (EditText) findViewById(R.id.inputArtist);
        inputUrl = (EditText) findViewById(R.id.inputUrl);

        btnAttach = (Button) findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUrl = inputUrl.getText().toString();
                String videoId = extractVideoId(musicUrl);
                youTubePlayer.loadVideo(videoId,1);
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                musicName = inputMusic.getText().toString();
                musicArtist = inputArtist.getText().toString();
                musicUrl = inputUrl.getText().toString();
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

                postData(url_create_music, params_create);
            }
        });
    }
    public void postData(String url, Map params) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in updating database",
                                    Toast.LENGTH_LONG).show();
                        }
                        if (response.equals("Success")) {
                            Toast.makeText(getApplicationContext(), "Success in updating database",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), KaraSession.class);
                            startActivity(intent);
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
        Intent intent = new Intent(AddMusic.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        startActivity(intent);
    }
}