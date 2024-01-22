package mdad.networkdata.karaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class YoutubeAttach extends AppCompatActivity {

    String uid,is_staff;
    Boolean is_staffBoolean;
    WebView webView;
    Button InsertLink;
    WebSettings webSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_attach);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        is_staffBoolean = is_staff.equals("1");

        webView = findViewById(R.id.webViewYoutube);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Load the clicked URL in the WebView itself
                view.loadUrl(url);
                return true;
            }
        });
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("https://www.youtube.com/");
        }






        InsertLink = (Button) findViewById(R.id.btnInsertLink);
        InsertLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String currentUrl = webView.getUrl();
                Intent youtube = new Intent(YoutubeAttach.this,AddMusic.class);
                youtube.putExtra("uid", uid);
                youtube.putExtra("is_staff", is_staff);
                youtube.putExtra("currentUrl", currentUrl);

                startActivity(youtube);
            }
        });
    }









    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }


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
        Intent intent = new Intent(YoutubeAttach.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        startActivity(intent);
    }







}