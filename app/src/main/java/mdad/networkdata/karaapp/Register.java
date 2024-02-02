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

public class Register extends AppCompatActivity {
    EditText inputEmail, inputPassword, inputConfirmPassword, inputNickName;
    String email, password, confirmPassword, nickName;
    Button btnSubmitRegister, btnCancel;
    private static String url_create_user = MainMenu.ipBaseAddress+"create_userVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

        inputEmail = (EditText) findViewById(R.id.inputRegisterEmail);
        inputPassword = (EditText) findViewById(R.id.inputRegisterPassword);
        inputConfirmPassword = (EditText) findViewById(R.id.inputRegisterConfirmPassword);
        inputNickName = (EditText) findViewById(R.id.inputRegisterNickName);

        btnSubmitRegister = (Button) findViewById(R.id.btnSubmitRegister);
        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                confirmPassword = inputConfirmPassword.getText().toString();
                nickName = inputNickName.getText().toString();

                if (!password.equals(confirmPassword)){
                    Toast.makeText(getApplicationContext(), "Password is not same as Confirm Password",
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String,String> params_create = new HashMap<String, String>();
                params_create.put("email", email);
                params_create.put("password", password);
                params_create.put("name", nickName);
                params_create.put("is_staff", "0");

                postData(url_create_user, params_create);
            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), Login.class);
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
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
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