package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ProfileSettings extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid, is_staff, username;
    private Boolean is_staffBoolean;
//    Declaration of String for EditText input.
    private String email, verifyPassword, password, confirmPassword, nickName;
//    Declaration of components from ProfileSettings's xml
    private EditText inputUpdateEmail, inputUpdateVerifyPassword, inputUpdatePassword, inputUpdateConfirmPassword, inputUpdateNickname;
    private Button btnSubmitUpdateUser, btnCancelEdit;
//    Declaration of Url address and requestType for postData
    private static String url_verify_user = MainMenu.ipBaseAddress+"verify_userVolley.php";
    private static String url_update_user = MainMenu.ipBaseAddress+"update_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int verify_user = 1, update_user = 2, update_device = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_profile_settings);
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

//        Declaration of EditText components tide to corresponding EditText id
        inputUpdateEmail = findViewById(R.id.inputUpdateEmail);
        inputUpdateVerifyPassword = findViewById(R.id.inputUpdateVerifyPassword);
        inputUpdatePassword = findViewById(R.id.inputUpdatePassword);
        inputUpdateConfirmPassword = findViewById(R.id.inputUpdateConfirmPassword);
        inputUpdateNickname = findViewById(R.id.inputUpdateNickName);

//        Button Submit to update user in database
        btnSubmitUpdateUser = findViewById(R.id.btnSubmitUpdateUser);
        btnSubmitUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Retrieve inputs from corresponding components
                email = inputUpdateEmail.getText().toString();
                verifyPassword = inputUpdateVerifyPassword.getText().toString();
                password = inputUpdatePassword.getText().toString();
                confirmPassword = inputUpdateVerifyPassword.getText().toString();
                nickName = inputUpdateNickname.getText().toString();
//                Store inputs into Map parameter to perform postData on verify with update
                Map<String, String> params_verify = new HashMap<String, String>();
                params_verify.put("email", email);
                params_verify.put("password", verifyPassword);
                postData(url_verify_user, params_verify, verify_user);
            }
        });

//        Button Cancel to navigate back to MainMenu
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

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
//                        Response specified for verify user response
                        if (requestType == verify_user) {
//                            Handling if user wrong password
                            if (response.equals("Error password")) {
                                Toast.makeText(getApplicationContext(), "Wrong password",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Handling if user wrong email
                            if (response.equals("Error email")) {
                                Toast.makeText(getApplicationContext(), "Wrong email",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Store result into String array and variables within this response
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");
//                            Ensure user result received
                            if (details.length > 0) {
//                                If user is not updating their password
                                if (password.isEmpty()){
                                    Map<String, String> params_update = new HashMap<String, String>();
                                    params_update.put("uid", uid);
                                    params_update.put("email", email);
                                    params_update.put("name", nickName);
                                    params_update.put("is_staff", is_staff);
                                    postData(url_update_user, params_update, update_user);
                                }
//                                If user is updating their password check password match confirmPassword
                                if (password.equals(confirmPassword)) {
                                    Map<String, String> params_update = new HashMap<String, String>();
                                    params_update.put("uid", uid);
                                    params_update.put("email", email);
                                    params_update.put("password", password);
                                    params_update.put("name", nickName);
                                    params_update.put("is_staff", is_staff);
                                    postData(url_update_user, params_update, update_user);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Password is not same as Confirm Password",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
//                            Response specified for updating user response
                        } if (requestType == update_user){
//                            email should not match any other user id's email
                            if (response.equals("Error, email already exists")){
                                Toast.makeText(getApplicationContext(), "Error, email already exists",
                                        Toast.LENGTH_LONG).show();
                            }
//                            Handling error in php
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                            }
//                            Handling success updating user
                            if (response.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database",
                                        Toast.LENGTH_LONG).show();
//                                Upon Success, navigate back to MainMenu
                                finish();
                                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                intent.putExtra("username", username);
                                startActivity(intent);
                            }
//                        Response specified for updating device if user log out
                        } if (requestType == update_device){
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

//    Option Menu Item select listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//         Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        Class<?>[] destinationClasses = {MainMenu.class, History.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class};
//        Iterate over menu items and check conditions
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
//        If the selected item is not found in the loop, fallback to super.onOptionsItemSelected
        return super.onOptionsItemSelected(item);
    }

//    Dynamic function for intent
    private void startActivityIntent(Class<?> cls) {
        Intent intent = new Intent(ProfileSettings.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}