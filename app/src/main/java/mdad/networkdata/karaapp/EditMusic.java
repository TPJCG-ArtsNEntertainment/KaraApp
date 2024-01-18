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
    String uid, is_staff, mid, music_name, music_artist, music_url, created_at, created_by, is_played;
    EditText editTextMusic, editTextArtist, editTextUrl;
    Button btnEditAttach, btnEditSubmit;
    YouTubePlayer youTubePlayer;
    private boolean isFullscreen = false, is_staffBoolean;
    private final int get_music_details = 1, update_music = 2;
    private static final String url_music_details = KaraSession.ipBaseAddress+"get_music_detailsVolley.php";
    private static final String url_update_product = KaraSession.ipBaseAddress+"update_musicVolley.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_music);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextMusic = (EditText) findViewById(R.id.inputEditMusic);
        editTextArtist = (EditText) findViewById(R.id.inputEditArtist);
        editTextUrl = (EditText) findViewById(R.id.inputEditUrl);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");
        mid = intent.getStringExtra("mid");
        Map<String,String> param_mid = new HashMap<String, String>();
        param_mid.put("mid",mid);

        postData(url_music_details, param_mid, get_music_details);

//        YoutubePlayer Logic
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        LinearLayout linearLayout = findViewById(R.id.editPageLinearLayout);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.editPageYoutubePlayer);
        FrameLayout fullscreenViewContainer = findViewById(R.id.editPageFullScreenViewContainer);
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
                    Window window = EditMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(false);
                    window.setNavigationBarColor(EditMusic.this.getResources().getColor(android.R.color.black));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) EditMusic.this).getSupportActionBar();
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
                    Window window = EditMusic.this.getWindow();
                    window.setDecorFitsSystemWindows(true);
                    window.setNavigationBarColor(EditMusic.this.getResources().getColor(android.R.color.transparent));
                } else {
                    ActionBar actionBar = ((AppCompatActivity) EditMusic.this).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            }
        });
        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                EditMusic.this.youTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo(extractVideoId(music_url), 0f);
            }
        }, iFramePlayerOptions);
        getLifecycle().addObserver(youTubePlayerView);
//        End of Logic

        btnEditAttach = (Button) findViewById(R.id.btnEditAttach);
        btnEditAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String musicUrl = editTextUrl.getText().toString();
                String videoId = extractVideoId(musicUrl);
                youTubePlayer.loadVideo(videoId,1);
            }
        });

        btnEditSubmit = (Button) findViewById(R.id.btnEditSubmit);
        btnEditSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music_name=editTextMusic.getText().toString();
                music_artist=editTextArtist.getText().toString();
                music_url=editTextUrl.getText().toString();
                Map<String, String> params_update = new HashMap<String, String>();
                params_update.put("mid",mid);
                params_update.put("music_name",music_name);
                params_update.put("artist_name", music_artist);
                params_update.put("url", music_url);
                params_update.put("is_played", is_played);

                postData(url_update_product,params_update,update_music);
            }
        });
    }
    public void postData(String url, Map params, final int requestType) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (requestType == get_music_details) {
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in accessing database", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] music = response.split("\\|");
                            String[] details = music[0].split(";");
                            mid = details[0];
                            music_name = details[1];
                            music_artist = details[2];
                            music_url = details[3];
                            created_at = details[4];
                            created_by = details[5];
                            is_played = details[6];

                            editTextMusic.setText(music_name);
                            editTextArtist.setText(music_artist);
                            editTextUrl.setText(music_url);
                        }
                        if (requestType == update_music) {
                            System.out.println(response);
                            if (response.trim().equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                            }
                            if (response.trim().equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
                                finish();
                                Intent intent = new Intent(getApplicationContext(), KaraSession.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                startActivity(intent);
                            }
                        }
                    }
                },
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
        Intent intent = new Intent(EditMusic.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        startActivity(intent);
    }
}