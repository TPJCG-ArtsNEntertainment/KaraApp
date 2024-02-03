package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.HashMap;
import java.util.Map;

public class MainMenu extends AppCompatActivity {
//    Declaration of base API address to fetch database
    public static String ipBaseAddress = "http://aetpjcgkara.atspace.cc/";
//    Declaration of String and Boolean for getIntent(). Public purpose is to use on Session fragment
    public static String uid, is_staff, username;
    public static Boolean is_staffBoolean;
//    Declaration of parent component and adapter for fragment
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
//    Declaration of String array to store fragment tablayout items
    private String[] titles = new String[]{"Session", "Player", "Lyrics"};
    private static final int NUM_PAGES = 3;
//    Declaration of Url address for postData
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_main);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Request external storage reading for music player
        MainMenu.this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);

//        Get Variables from previous page
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

//        Declaration of fragment parent layout
        viewPager = findViewById(R.id.mypager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        TabLayoutMediator tlm = new TabLayoutMediator(tabLayout,viewPager,(tab, position) ->
                tab.setText(titles[position]));
        tlm.attach();
    }

//    Customization of fragment parent Adapter
    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(FragmentActivity fa) {
            super(fa);
        }
//        Set each fragment navigate to corresponding fragment
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: {
                    return Session.newInstance("session",null);
                }
                case 1: {
                    return Player.newInstance("player", null);
                }
                case 2: {
                    return Lyrics.newInstance("lyrics", null);
                }
                default:
                    return new Fragment();
            }
        }
//        Retrieve current page of fragment
        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

//    ---------- PostData Volley Function ----------
    public void postData(String url, Map params) {
//        Declaration of volley request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        Declaration of string request for post parameters
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            Upon receiving response, actions to be done.
//            This postData only used for LogOut purpose in this page
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
//        Inflate the option menu and display the option items when clicked;
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
        Intent intent = new Intent(MainMenu.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }

}