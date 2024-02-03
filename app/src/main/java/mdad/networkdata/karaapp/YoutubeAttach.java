package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeAttach extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid,is_staff,username,intent_from_activity,mid,is_played,intent_from;
    private Boolean is_staffBoolean;
//    Declaration of String for youtube video detail.
    private String attachName,attachArtist;
//    Declaration of components from YoutubeAttach's xml
    private WebView webView;
    private Button InsertLink;
//    Declaration of Url address postData
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_youtube_attach);
//        Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Get Variables from previous page
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");
        intent_from_activity = intent.getStringExtra("intent_from_activity");
        mid = intent.getStringExtra("mid");
        is_played = intent.getStringExtra("is_played");
        intent_from = intent.getStringExtra("intent_from");

//        WebView to display youtube webpage
        webView = findViewById(R.id.webViewYoutube);
//        Set webView hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        Set webView loading priority
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
//        Load url in clicked inside this webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Load the clicked URL in the WebView itself
                view.loadUrl(url);
                return true;
            }
        });

//        Prevent Url fixed in same page
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("https://www.youtube.com/");
        }

//        Button Insert Link to retrieve the youtube video details
        InsertLink = findViewById(R.id.btnInsertLink);
        InsertLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Retrieve video name and author with javascript
                webView.evaluateJavascript(
                        "(function() {" +
                                "   var scriptTag = document.querySelector('script[type=\"application/ld+json\"]');" +
                                "   if (scriptTag) {" +
                                "       var jsonContent = JSON.parse(scriptTag.textContent);" +
                                "       return jsonContent.name + '|||' + jsonContent.author;" +
                                "   } else {" +
                                "       return null;" +
                                "   }" +
                                "})();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String result) {
                                if (result != null) {
                                    String[] values = result.split("\\|\\|\\|");
                                    if (values.length == 2) {
                                        attachName = values[0].substring(1);;
                                        attachArtist = values[1].substring(0, values[1].length() - 1);;
                                    }
//                                    Navigate back to correct previous page with information
                                    if (intent_from.equals("AddMusic")){
                                        finish();
                                        Intent youtube = new Intent(YoutubeAttach.this, AddMusic.class);
                                        youtube.putExtra("uid", uid);
                                        youtube.putExtra("is_staff", is_staff);
                                        youtube.putExtra("username", username);
                                        youtube.putExtra("intent_from_activity", intent_from_activity);
                                        youtube.putExtra("attachUrl", webView.getUrl());
                                        youtube.putExtra("attachName", attachName);
                                        youtube.putExtra("attachArtist", attachArtist);
                                        startActivity(youtube);
                                    } if (intent_from.equals("EditMusic")){
                                        finish();
                                        Intent youtube = new Intent(YoutubeAttach.this, EditMusic.class);
                                        youtube.putExtra("uid", uid);
                                        youtube.putExtra("is_staff", is_staff);
                                        youtube.putExtra("username", username);
                                        youtube.putExtra("mid",mid);
                                        youtube.putExtra("is_played",is_played);
                                        youtube.putExtra("intent_from", "YoutubeAttach");
                                        youtube.putExtra("attachUrl", webView.getUrl());
                                        youtube.putExtra("attachName", attachName);
                                        youtube.putExtra("attachArtist", attachArtist);
                                        startActivity(youtube);
                                    }
//                                    Log if the javascript failed to retrieve video name and author
                                } else {
                                    Log.e("Title", "Title element not found in the HTML");
                                }
                            }
                        });
            }
        });
    }

//  Save webview state prevent crash occur lost of webview state
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
//    Restore saved webview state
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

//    ---------- PostData Volley Function ----------
    public void postData(String url, Map params) {
//        Declaration of volley request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        Declaration of string request for post parameters
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                Upon receiving response, actions to be done.
//                This postData only used for Log Out purpose in this page
            @Override
            public void onResponse(String response) {
//                Handling error in php
                if (response.equals("Error, false request.")) {
                    Toast.makeText(getApplicationContext(), "Error in verify device",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
                startActivityIntent(Login.class);
            }
//            Error Listener if Volley failed to fetch data with database
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
//        perform Volley Request
        requestQueue.add(stringRequest);
    }

//   --------------- General Functions for option menu navigation for Action Bar --------------
//    Create Option Menu in Action Bar
    @Override
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
//        Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        Class<?>[] destinationClasses = {MainMenu.class, History.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class};
//        Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
//              item6 is Log out, therefore updating device is needed
            if (id == R.id.item6){
                Map<String, String> param_update = new HashMap<>();
                param_update.put("uid", uid);
                param_update.put("token", "");
                postData(url_update_device, param_update);
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
//        If the selected item is not found in the loop, fallback to super.onOptionsItemSelected
        return super.onOptionsItemSelected(item);
    }

//    Dynamic function for intent
    private void startActivityIntent(Class<?> cls) {
        Intent intent = new Intent(YoutubeAttach.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}