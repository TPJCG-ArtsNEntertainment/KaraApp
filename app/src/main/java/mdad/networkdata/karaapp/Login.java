package mdad.networkdata.karaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
    EditText inputEmail, inputPassword;
    Button btnLogin, btnRegister;
    private static String url_verify_user = MainMenu.ipBaseAddress+"verify_userVolley.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                postData(url_verify_user, param_login);
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
    public void postData(String url, Map params) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                        String uid = details[0];
                        String email = details[1];
                        String password = details[2];
                        String name = details[3];
                        String is_staff = details[4];

                        if (name == null){
                            Toast.makeText(getApplicationContext(), "Logging in to"+email,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Logging in to"+name,
                                    Toast.LENGTH_LONG).show();
                        }
                        finish();
                        Intent intent = new Intent(Login.this, MainMenu.class);
                        intent.putExtra("uid", uid);
                        intent.putExtra("is_staff", is_staff);
                        startActivity(intent);
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