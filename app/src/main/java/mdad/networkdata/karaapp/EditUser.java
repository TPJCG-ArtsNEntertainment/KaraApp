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

public class EditUser extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid, is_staff, username, user_id, user_email, user_nickName, user_is_staff;
    private Boolean is_staffBoolean;
//    Declaration of components from EditUser's xml
    private EditText inputEditEmail, inputEditPassword, inputEditConfirmPassword, inputEditNickName;
    private Button btnSubmitEditUser, btnCancelEdit;
//    Declaration of Url address and requestType for postData
    private static String url_update_user = MainMenu.ipBaseAddress+"update_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int update_user=1, update_device=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_edit_user);
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
//        Get user variables from UserManagement page
        user_id = intent.getStringExtra("user_id");
        user_email = intent.getStringExtra("email");
        user_nickName = intent.getStringExtra("user_nickName");
        user_is_staff = intent.getStringExtra("user_is_staff");

//        Declaration of EditText components tide to corresponding EditText id
        inputEditEmail = findViewById(R.id.inputEditEmail);
        inputEditPassword = findViewById(R.id.inputEditPassword);
        inputEditConfirmPassword = findViewById(R.id.inputEditConfirmPassword);
        inputEditNickName = findViewById(R.id.inputEditNickName);

//        Initialize text into EditText from UserManagement
        inputEditEmail.setText(user_email);
        inputEditNickName.setText(user_nickName);

//        Button Submit to update user in database
        btnSubmitEditUser = (Button) findViewById(R.id.btnSubmitEditUser);
        btnSubmitEditUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Retrieve inputs from corresponding components
                String inputEmail = inputEditEmail.getText().toString();
                String inputPassword = inputEditPassword.getText().toString();
                String inputConfirmPassword = inputEditConfirmPassword.getText().toString();
                String inputNickName = inputEditNickName.getText().toString();

//                Check if staff is updating password
                if (!inputPassword.equals("")){
//                    Check if password and confirmPassword are match
                    if (inputPassword.equals(inputConfirmPassword)) {
//                        Store inputs into Map parameter to perform postData
                        Map<String, String> params_update = new HashMap<String, String>();
                        params_update.put("uid", user_id);
                        params_update.put("email", inputEmail);
                        params_update.put("password", inputPassword);
                        params_update.put("name", inputNickName);
                        params_update.put("is_staff", user_is_staff);
                        postData(url_update_user, params_update, update_user);
                    } else {
                        Toast.makeText(getApplicationContext(), "Password is not same as Confirm Password",
                                Toast.LENGTH_LONG).show();
                    }
//                    If staff is not updating password
                } else {
//                    Store inputs into Map parameter to perform postData
                    Map<String, String> params_update = new HashMap<String, String>();
                    params_update.put("uid", user_id);
                    params_update.put("email", inputEmail);
                    params_update.put("name", inputNickName);
                    params_update.put("is_staff", user_is_staff);
                    postData(url_update_user, params_update, update_user);
                }
            }
        });

//        Button cancel to navigate back to User Management
        btnCancelEdit = (Button) findViewById(R.id.btnCancelEdit);
        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), UserManagement.class);
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                Upon receiving response, actions to be done.
            @Override
            public void onResponse(String response) {
//                Response specified for update user response
                if (requestType == update_user){
//                    Handling if email registered for another user
                    if (response.equals("Error, email already exists")){
                        Toast.makeText(getApplicationContext(), "Error, email already exists",
                                Toast.LENGTH_LONG).show();
                    }
//                    Handling error from php
                    if (response.equals("Error")) {
                        Toast.makeText(getApplicationContext(), "Error in updating database",
                                Toast.LENGTH_LONG).show();
                    }
//                    Handling success result
                    if (response.equals("Success")) {
                        Toast.makeText(getApplicationContext(), "Success in updating database",
                                Toast.LENGTH_LONG).show();
//                        Upon Success, navigate back to UserManagement
                        finish();
                        Intent intent = new Intent(getApplicationContext(), UserManagement.class);
                        intent.putExtra("uid", uid);
                        intent.putExtra("is_staff", is_staff);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                }
//                Response specified for updating device if user log out
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
        Intent intent = new Intent(EditUser.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}