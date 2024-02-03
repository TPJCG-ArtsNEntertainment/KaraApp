package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

public class Login extends AppCompatActivity {
//    Declaration of String to store user detail
    private String uid, is_staff, username, androidId;
//    Declaration of components from Login's xml
    private EditText inputEmail, inputPassword;
    private Button btnLogin, btnRegister;
//    Declaration of Url address and requestType for postData
    private static String url_verify_device = MainMenu.ipBaseAddress + "verify_deviceVolley.php";
    private static String url_verify_user = MainMenu.ipBaseAddress+"verify_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int verify_device = 1, verify_user =2, update_device=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_login);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Retrieve device's Android Id to check if any user login in this device previously
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Map<String, String> param_verify = new HashMap<>();
        param_verify.put("token", androidId);
        postData(url_verify_device, param_verify, verify_device);

//        Declaration of EditText components tide to corresponding EditText id
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);

//        Button Login to verify user
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Retrieve inputs from corresponding components
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
//                Store inputs into Map parameter to perform postData
                Map<String, String> param_login = new HashMap<>();
                param_login.put("email", email);
                param_login.put("password", password);
                param_login.put("token", androidId);
                postData(url_verify_user, param_login, verify_user);
            }
        });

//        Button Register to navigate to Register Page
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
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
//                        Response specified for verify user's device response
                        if (requestType == verify_device){
//                            Handling error in php
                            if (response.equals("Error, false request.")) {
                                Toast.makeText(getApplicationContext(), "Error in verify device",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Handling error if this device has no tide to any user
                            if (response.equals("Error: Device not registered.")) {
                                return; //Account not logged in before.
                            }
//                            Store result into String array and variables within this response flow
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");
                            String login_uid = details[0];
                            String login_email = details[1];
                            String login_password = details[2];
                            String login_name = details[3];
                            String login_is_staff = details[4];
                            String login_token = details[5];

//                            Set detail into variable
                            uid = login_uid;
                            is_staff = login_is_staff;

//                            Navigate to MainMenu upon success
                            finish();
                            Intent intent = new Intent(Login.this, MainMenu.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("is_staff", is_staff);
//                            Check user preferred name and welcome user
                            if (login_name.isEmpty()) username = login_email;
                            else username = login_name;
                            intent.putExtra("username", username);
                            Toast.makeText(getApplicationContext(), "Logging in to "+username,
                                    Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        }
//                        Response specified for verify user login response
                        if (requestType == verify_user){
//                            Handling if user wrong password
                            if (response.equals("Error password")) {
                                Toast.makeText(getApplicationContext(), "Wrong password",
                                        Toast.LENGTH_LONG).show();
                                return;
//                            Handling if user wrong email / email does not exist in database
                            }if (response.equals("Error email")) {
                                Toast.makeText(getApplicationContext(), "Wrong email",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
//                            Store result into String array and variables within this response
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");
                            String login_uid = details[0];
                            String login_email = details[1];
                            String login_password = details[2];
                            String login_name = details[3];
                            String login_is_staff = details[4];

//                            Set detail into variable
                            uid = login_uid;
                            is_staff = login_is_staff;

//                            Update this user device id to database
                            Map<String, String> param_update = new HashMap<>();
                            param_update.put("uid",login_uid);
                            param_update.put("token", androidId);
                            postData(url_update_device, param_update, update_device);

//                            Navigate to MainMenu upon success
                            finish();
                            Intent intent = new Intent(Login.this, MainMenu.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("is_staff", is_staff);
//                            Check user preferred name and welcome user
                            if (login_name == null) username = login_email;
                            else username = login_name;
                            intent.putExtra("username", username);
                            Toast.makeText(getApplicationContext(), "Logging in to "+username,
                                    Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        }
//                        Response specified for updating device
                        if (requestType == update_device){
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                            }
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
}