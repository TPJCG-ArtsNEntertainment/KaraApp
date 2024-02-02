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
    String uid,is_staff,username,user_id,user_email,user_nickName,user_is_staff;
    Boolean is_staffBoolean;
    EditText inputEditEmail, inputEditPassword, inputEditConfirmPassword, inputEditNickName;
    Button btnSubmitEditUser, btnCancelEdit;
    private static String url_update_user = MainMenu.ipBaseAddress+"update_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int update_user=1, update_device=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        user_id = intent.getStringExtra("user_id");
        user_email = intent.getStringExtra("email");
        user_nickName = intent.getStringExtra("user_nickName");
        user_is_staff = intent.getStringExtra("user_is_staff");

        inputEditEmail = (EditText) findViewById(R.id.inputEditEmail);
        inputEditPassword = (EditText) findViewById(R.id.inputEditPassword);
        inputEditConfirmPassword = (EditText) findViewById(R.id.inputEditConfirmPassword);
        inputEditNickName = (EditText) findViewById(R.id.inputEditNickName);

        inputEditEmail.setText(user_email);
        inputEditNickName.setText(user_nickName);

        btnSubmitEditUser = (Button) findViewById(R.id.btnSubmitEditUser);
        btnSubmitEditUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmail = inputEditEmail.getText().toString();
                String inputPassword = inputEditPassword.getText().toString();
                String inputConfirmPassword = inputEditConfirmPassword.getText().toString();
                String inputNickName = inputEditNickName.getText().toString();

                if (!inputPassword.equals("")){
                    if (inputPassword.equals(inputConfirmPassword)) {
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
                } else {
                    Map<String, String> params_update = new HashMap<String, String>();
                    params_update.put("uid", user_id);
                    params_update.put("email", inputEmail);
                    params_update.put("name", inputNickName);
                    params_update.put("is_staff", user_is_staff);
                    postData(url_update_user, params_update, update_user);
                }
            }
        });
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

    public void postData(String url, Map params, final int requestType) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (requestType == update_user){
                    System.out.println(response);
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
                        intent.putExtra("username", username);
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
        Intent intent = new Intent(EditUser.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}