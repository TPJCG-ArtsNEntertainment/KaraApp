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
//    Declaration of String to store user detail
    private String email, password, confirmPassword, nickName;
//    Declaration of components from Register's xml
    private EditText inputEmail, inputPassword, inputConfirmPassword, inputNickName;
    private Button btnSubmitRegister, btnCancel;
//    Declaration of Url address for postData
    private static String url_create_user = MainMenu.ipBaseAddress+"create_userVolley.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_register);
//         Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Declaration of EditText components tide to corresponding EditText id
        inputEmail = findViewById(R.id.inputRegisterEmail);
        inputPassword = findViewById(R.id.inputRegisterPassword);
        inputConfirmPassword = findViewById(R.id.inputRegisterConfirmPassword);
        inputNickName = findViewById(R.id.inputRegisterNickName);

//        Button Register to create user
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Retrieve inputs from corresponding components
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                confirmPassword = inputConfirmPassword.getText().toString();
                nickName = inputNickName.getText().toString();

//                Check if password match confirmPassword
                if (!password.equals(confirmPassword)){
                    Toast.makeText(getApplicationContext(), "Password is not same as Confirm Password",
                            Toast.LENGTH_LONG).show();
                    return;
//                Check if compulsory field is empty
                } else if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No fields must be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                }
//                Store inputs into Map parameter to perform postData
                Map<String,String> params_create = new HashMap<String, String>();
                params_create.put("email", email);
                params_create.put("password", password);
                params_create.put("name", nickName);
                params_create.put("is_staff", "0");
                postData(url_create_user, params_create);
            }
        });

//        Button Cancel to navigate back to Login page
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });
    }

//    ---------- PostData Volley Function ----------
    public void postData(String url, Map params) {
//        Declaration of volley request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        Declaration of string request for post parameters
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                Upon receiving response, actions to be done.
//                This postData only used for create user purpose in this page
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                          email should not match any other user id's email
                        if (response.equals("Error, email already exists")){
                            Toast.makeText(getApplicationContext(), "Error, email already exists",
                                    Toast.LENGTH_LONG).show();
                        }
//                          Handling error in php
                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in updating database",
                                    Toast.LENGTH_LONG).show();
                        }
//                          Handle Success. Navigate back to Login page
                        if (response.equals("Success")) {
                            Toast.makeText(getApplicationContext(), "Success in updating database",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
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