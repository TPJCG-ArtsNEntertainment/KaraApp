package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

public class AddUser extends AppCompatActivity {
    EditText inputCreateEmail, inputCreatePassword, inputCreateConfirmPassword, inputCreateNickName;
    Button btnSubmitCreateUser, btnCancelCreate;
    CheckBox isStaffCheckBox;
    String email, password, confirmPassword, nickName, user_is_staff, uid, is_staff, username;
    Boolean is_staffBoolean;
    private static String url_create_user = MainMenu.ipBaseAddress+"create_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int create_user=1, update_device=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        inputCreateEmail = (EditText) findViewById(R.id.inputCreateEmail);
        inputCreatePassword = (EditText) findViewById(R.id.inputCreatePassword);
        inputCreateConfirmPassword = (EditText) findViewById(R.id.inputCreateConfirmPassword);
        inputCreateNickName = (EditText) findViewById(R.id.inputCreateNickName);
        isStaffCheckBox = (CheckBox) findViewById(R.id.isStaffCheckBox);

        btnSubmitCreateUser = (Button) findViewById(R.id.btnSubmitCreateUser);
        btnSubmitCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = inputCreateEmail.getText().toString();
                password = inputCreatePassword.getText().toString();
                confirmPassword = inputCreateConfirmPassword.getText().toString();
                nickName = inputCreateNickName.getText().toString();
                user_is_staff = isStaffCheckBox.isChecked() ? "1" : "0";

                if(password.equals(confirmPassword)){
                    Map<String, String> params_create = new HashMap<String, String>();
                    params_create.put("email", email);
                    params_create.put("password", password);
                    params_create.put("name", nickName);
                    params_create.put("is_staff", user_is_staff);
                    postData(url_create_user, params_create, create_user);
                }
            }
        });
        btnCancelCreate = (Button) findViewById(R.id.btnCancelCreate);
        btnCancelCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), UserManagement.class);
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
                        if (requestType == create_user){
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
                                Intent intent = new Intent(getApplicationContext(), UserManagement.class);
                                intent.putExtra("uid", uid);
                                intent.putExtra("is_staff", is_staff);
                                startActivity(intent);
                            }
                        }
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
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
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
        Intent intent = new Intent(AddUser.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}