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
    EditText inputUpdateEmail, inputUpdateVerifyPassword, inputUpdatePassword, inputUpdateConfirmPassword, inputUpdateNickname;
    Button btnSubmitUpdateUser, btnCancelEdit;
    String email, verifyPassword, password, confirmPassword, nickName, uid, is_staff, username;
    Boolean is_staffBoolean, verified;
    private final int verify_user=1, update_user=2, update_device=3;
    private static String url_verify_user = MainMenu.ipBaseAddress+"verify_userVolley.php";
    private static String url_update_user = MainMenu.ipBaseAddress+"update_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        inputUpdateEmail = (EditText) findViewById(R.id.inputUpdateEmail);
        inputUpdateVerifyPassword = (EditText) findViewById(R.id.inputUpdateVerifyPassword);
        inputUpdatePassword = (EditText) findViewById(R.id.inputUpdatePassword);
        inputUpdateConfirmPassword = (EditText) findViewById(R.id.inputUpdateConfirmPassword);
        inputUpdateNickname = (EditText) findViewById(R.id.inputUpdateNickName);

        btnSubmitUpdateUser = (Button) findViewById(R.id.btnSubmitUpdateUser);
        btnSubmitUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = inputUpdateEmail.getText().toString();
                verifyPassword = inputUpdateVerifyPassword.getText().toString();
                password = inputUpdatePassword.getText().toString();
                confirmPassword = inputUpdateVerifyPassword.getText().toString();
                nickName = inputUpdateNickname.getText().toString();

                Map<String, String> params_verify = new HashMap<String, String>();
                params_verify.put("email", email);
                params_verify.put("password", verifyPassword);
                postData(url_verify_user, params_verify, verify_user);
                if (verified.equals(true)){
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
            }
        });
        btnCancelEdit = (Button) findViewById(R.id.btnCancelEdit);
        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), Session.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                startActivity(intent);
            }
        });
    }

    public void postData(String url, Map params, final int requestType) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (requestType == verify_user) {
                            if (response.equals("Error password")) {
                                Toast.makeText(getApplicationContext(), "Wrong password",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (response.equals("Error email")) {
                                Toast.makeText(getApplicationContext(), "Wrong email",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");

                            if (details.length > 1) {
                                verified = true;
                            } else {
                                verified = false;
                            }
                        } if (requestType == update_user){
                            if (response.equals("Error, email already exists")){
                                Toast.makeText(getApplicationContext(), "Error, email already exists",
                                        Toast.LENGTH_LONG).show();
                            }
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                            }
                            if (response.equals("Success")) {
                                Toast.makeText(getApplicationContext(), "Success in updating database",
                                        Toast.LENGTH_LONG).show();
                                finish();
                                Intent intent = new Intent(getApplicationContext(), Session.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                startActivity(intent);
                            }
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
                postData(url_update_device, param_update, update_device);
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
        Intent intent = new Intent(ProfileSettings.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}