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
    private String uid, is_staff, username, androidId;
    private EditText inputEmail, inputPassword;
    private Button btnLogin, btnRegister;
    private static String url_verify_device = MainMenu.ipBaseAddress + "verify_deviceVolley.php";
    private static String url_verify_user = MainMenu.ipBaseAddress+"verify_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int verify_device = 1, verify_user =2, update_device=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Map<String, String> param_verify = new HashMap<>();
        param_verify.put("token", androidId);
        postData(url_verify_device, param_verify, verify_device);

        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputPassword = (EditText) findViewById(R.id.inputPassword);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                Map<String, String> param_login = new HashMap<>();
                param_login.put("email", email);
                param_login.put("password", password);
                param_login.put("token", androidId);
                postData(url_verify_user, param_login, verify_user);
            }
        });
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
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
                        if (requestType == verify_device){
                            if (response.equals("Error, false request.")) {
                                Toast.makeText(getApplicationContext(), "Error in verify device",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (response.equals("Error: Device not registered.")) {
                                return; //Account not logged in before.
                            }
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");
                            String login_uid = details[0];
                            String login_email = details[1];
                            String login_password = details[2];
                            String login_name = details[3];
                            String login_is_staff = details[4];
                            String login_token = details[5];

                            uid = login_uid;
                            is_staff = login_is_staff;

                            finish();
                            Intent intent = new Intent(Login.this, MainMenu.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("is_staff", is_staff);
                            if (login_name == null) username = login_email;
                            else username = login_name;
                            intent.putExtra("username", username);
                            Toast.makeText(getApplicationContext(), "Logging in to "+username,
                                    Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        }
                        if (requestType == verify_user){
                            if (response.equals("Error password")) {
                                Toast.makeText(getApplicationContext(), "Wrong password",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }if (response.equals("Error email")) {
                                Toast.makeText(getApplicationContext(), "Wrong email",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] user = response.split("\\|");
                            String[] details = user[0].split(";");
                            String login_uid = details[0];
                            String login_email = details[1];
                            String login_password = details[2];
                            String login_name = details[3];
                            String login_is_staff = details[4];
//                            String login_token = details[5];

                            uid = login_uid;
                            is_staff = login_is_staff;

                            Map<String, String> param_update = new HashMap<>();
                            param_update.put("uid",login_uid);
                            param_update.put("token", androidId);
                            postData(url_update_device, param_update, update_device);

                            finish();
                            Intent intent = new Intent(Login.this, MainMenu.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("is_staff", is_staff);
                            if (login_name == null) username = login_email;
                            else username = login_name;
                            intent.putExtra("username", username);
                            Toast.makeText(getApplicationContext(), "Logging in to "+username,
                                    Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        }
                        if (requestType == update_device){
                            if (response.equals("Error")) {
                                Toast.makeText(getApplicationContext(), "Error in updating database",
                                        Toast.LENGTH_LONG).show();
                            }
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
}