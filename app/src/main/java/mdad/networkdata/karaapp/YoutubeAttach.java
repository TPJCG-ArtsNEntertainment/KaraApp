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
    String uid,is_staff,username,mid,is_played,intent_from,attachName,attachArtist;
    Boolean is_staffBoolean;
    WebView webView;
    Button InsertLink;
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_attach);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");
        mid = intent.getStringExtra("mid");
        is_played = intent.getStringExtra("is_played");
        intent_from = intent.getStringExtra("intent_from");

        webView = findViewById(R.id.webViewYoutube);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setJavaScriptEnabled(true);
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
            public void onClick(View v) {
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        // Page has finished loading, now get the complete HTML
                        view.evaluateJavascript("document.documentElement.outerHTML;", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                // Log or process the complete HTML
                                Log.d("WebView", html);
                            }
                        });
                    }
                });
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
                                    if (intent_from.equals("AddMusic")){
                                        // Handle the title as needed, for example, pass it to another activity
                                        finish();
                                        Intent youtube = new Intent(YoutubeAttach.this, AddMusic.class);
                                        youtube.putExtra("uid", uid);
                                        youtube.putExtra("is_staff", is_staff);
                                        youtube.putExtra("username", username);
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
                                } else {
                                    Log.e("Title", "Title element not found in the HTML");
                                }
                            }
                        });
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
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};
        Class<?>[] destinationClasses = {MainMenu.class, History.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class};
        // Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
            if (id == R.id.item6){
                Map<String, String> param_update = new HashMap<>();
                param_update.put("uid", uid);
                param_update.put("token", "");
                postData(url_update_device, param_update);
            } else if (id == menuItems[i]) {
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
        intent.putExtra("username", username);
        startActivity(intent);
    }
    public void postData(String url, Map params) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("Error, false request.")) {
                    Toast.makeText(getApplicationContext(), "Error in verify device",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
                startActivityIntent(Login.class);
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
}