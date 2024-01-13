package mdad.networkdata.karaapp;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class FullscreenExampleActivity extends AppCompatActivity {

    private YouTubePlayer youTubePlayer;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_example);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
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
                fullscreenViewContainer.setVisibility(View.VISIBLE);
                fullscreenViewContainer.addView(fullscreenView);
            }

            @Override
            public void onExitFullscreen() {
                isFullscreen = false;

                youTubePlayerView.setVisibility(View.VISIBLE);
                fullscreenViewContainer.setVisibility(View.GONE);
                fullscreenViewContainer.removeAllViews();
            }
        });

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                FullscreenExampleActivity.this.youTubePlayer = youTubePlayer;
                youTubePlayer.loadVideo("S0Q4gqBUs7c", 0f);
            }
        }, iFramePlayerOptions);

        getLifecycle().addObserver(youTubePlayerView);
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
}
