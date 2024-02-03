package mdad.networkdata.karaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManagement extends AppCompatActivity {
//    Declaration of String and Boolean for getIntent().
    private String uid,is_staff,username;
    private Boolean is_staffBoolean;
//    Declaration of components from UserManagement's xml
    private ListView listViewUser;
    private SearchView userSearchView;
    private Button btnCreateUser;
//    Declaration of String to store selected item
    private String selectedUserId, selectedUserEmail, selectedUserName, selectedUserIsStaff;
//    Declaration of ArrayList with HashMap for user's listView
    private ArrayList<HashMap<String, String>> userList, originalUserList, filteredUserList, targetList;
//    Declaration of Url address and requestType for postData
    private static String url_get_all_users = MainMenu.ipBaseAddress+"get_all_userVolley.php";
    private static String url_update_userStaff = MainMenu.ipBaseAddress+"update_userStaffVolley.php";
    private static String url_delete_user = MainMenu.ipBaseAddress+"delete_userVolley.php";
    private static String url_update_device = MainMenu.ipBaseAddress+"update_deviceVolley.php";
    private final int get_all_users = 1, update_delete_user = 2, update_user_staff = 3, update_device = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set this onCreate tide to corresponding xml
        setContentView(R.layout.activity_user_management);
//        Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        Set the ActionBar title with the activity name
        setTitle(getClass().getSimpleName());

//        Get Variables from previous page
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

//        Declaration of ListView
        listViewUser = findViewById(R.id.listViewUser);
        registerForContextMenu(listViewUser);
//         ArrayList to store user info in Hashmap for ListView
        userList = new ArrayList<HashMap<String, String>>();
//         Perform postData to update ListView
        postData(url_get_all_users, null, get_all_users);

//        Get original ListAdapter from ListView's userList
        ListAdapter originalAdapter = new SimpleAdapter(
                UserManagement.this, userList,
                R.layout.list_view_users, new String[]{"uid","email", "name", "is_staff"},
                new int[]{R.id.uUid, R.id.uEmail, R.id.uName, R.id.uIsStaff}
        );
        originalUserList = userList;
        filteredUserList = new ArrayList<>(originalUserList);
        listViewUser.setAdapter(originalAdapter);

//        Declaration of Search bar, query tide to update ListView with filter
        userSearchView = findViewById(R.id.userSearchView);
        userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}
            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the items based on the search query
                filter(newText);
                return true;
            }
        });

//        Button Create User to navigate to AddUser Page
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserManagement.this, AddUser.class);
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
            @Override
            public void onResponse(String response) {
//                Response specified for retrieve all user response
                if (requestType == get_all_users) {
//                    check if error code received from server.
                    if (response.equals("Error")) {
                        Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }
//                    Clear userList
                    userList.clear();
//                    store each user from database in String array
                    String[] users = response.split("\\|");
//                    for each user, retrieve the user details
                    for (int i = 0; i < users.length; i++) {
//                        Storing each user info in variable
                        String[] details = users[i].split(";");
                        String uUid = details[0];
                        String uEmail = details[1];
                        String uPassword = details[2];
                        String uName = details[3];
                        String uIsStaff = details[4];

                        // HashMap for each user details
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("user_id", uUid);
                        map.put("email", uEmail);
                        map.put("password", uPassword);
                        map.put("name", uName);
                        map.put("is_staff", uIsStaff);

                        // adding map HashList to ArrayList
                        userList.add(map);
                    }
//                    populate the listview with user information from Hashmap
                    ListAdapter adapter = new SimpleAdapter(
                            getApplicationContext(), userList,
                            R.layout.list_view_users, new String[]{"user_id", "email", "name", "is_staff"}, new int[]{R.id.uUid, R.id.uEmail, R.id.uName, R.id.uIsStaff}
                    );
//                     Updating listview
                    listViewUser.setAdapter(adapter);
                }
//                Response specified for updating and deleting user from user list
                if (requestType == update_delete_user) {
                    if (response.equals("Error")) {
                        Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                    }
                    if (response.equals("Success")) {
                        Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();

//                        Refresh user list
                        postData(url_get_all_users, null, get_all_users);
                        ((BaseAdapter) listViewUser.getAdapter()).notifyDataSetChanged();
                    }
                }
//                Response specified for update user staff status response
                if (requestType == update_user_staff) {
                    if (response.equals("Error")) {
                        Toast.makeText(getApplicationContext(), "Error in updating database", Toast.LENGTH_LONG).show();
                    }
                    if (response.equals("Success")) {
                        Toast.makeText(getApplicationContext(), "Success in updating database", Toast.LENGTH_LONG).show();
                        postData(url_get_all_users, null, get_all_users);
                    }
                }
//                 Response specified for updating device if user log out
                if (requestType == update_device) {
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

//    --------------- General Functions for context menu navigation for ListView item --------------
//    Create Context Menu in ListView Item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        Inflate the context menu and display context menu when item long pressed
        UserManagement.this.getMenuInflater().inflate(R.menu.menu_user, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        Retrieve selected item data into HashMap
        HashMap<String, String> rowData = userList.get(info.position);
//        Toggle between option to set staff or user depends on item's is_staff status
        Boolean is_staff_currently = rowData.get("is_staff").equals("1");
        if (is_staff_currently){
            menu.findItem(R.id.option_set_user).setVisible(true);
            menu.findItem(R.id.option_set_staff).setVisible(false);
        } else {
            menu.findItem(R.id.option_set_user).setVisible(false);
            menu.findItem(R.id.option_set_staff).setVisible(true);
        }
    }

//    Context Menu Item select listener
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
//        Retrieve information from selected item
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        Set targetList based on filter status
        if (filteredUserList.isEmpty()) targetList = userList;
        else targetList = filteredUserList;
//        Retrieve selected item data into HashMap and variables
        HashMap<String, String> rowData = targetList.get(info.position);
        selectedUserId = rowData.get("user_id");
        selectedUserEmail = rowData.get("email");
        selectedUserName = rowData.get("name");
        selectedUserIsStaff = rowData.get("is_staff");
//        Handling each contextItem option selection
        int itemId = item.getItemId();
        if (itemId == R.id.option_set_staff) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("uid", selectedUserId);
            params_update.put("is_staff", "1");
            postData(url_update_userStaff, params_update, update_user_staff);
        } else if (itemId == R.id.option_set_user) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("uid", selectedUserId);
            params_update.put("is_staff", "0");
            postData(url_update_userStaff, params_update, update_user_staff);
        } else if (itemId == R.id.option_edit) {
            Intent intent = new Intent(UserManagement.this, EditUser.class);
            intent.putExtra("uid", MainMenu.uid);
            intent.putExtra("is_staff", MainMenu.is_staff);
            intent.putExtra("username", MainMenu.username);
            intent.putExtra("user_id", selectedUserId);
            intent.putExtra("email", selectedUserEmail);
            intent.putExtra("user_nickName", selectedUserName);
            intent.putExtra("user_is_staff", selectedUserIsStaff);
            startActivity(intent);
        } else if (itemId == R.id.option_remove) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("uid", selectedUserId);
            postData(url_delete_user, params_update, update_delete_user);
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

//  ------------- General function apply for Search bar to the corresponding List----------
    private void filter(String query) {
//        Initialize filter list
        filteredUserList.clear();
        if (TextUtils.isEmpty(query)) {
//            If the query is empty, show all items
            filteredUserList.addAll(originalUserList);
        } else {
//            Filter items based on the query
            for (HashMap<String, String> user : originalUserList) {
                if (user.get("user_id").toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                } else if (user.get("email").toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                } else if (user.get("name").toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                } else if (user.get("is_staff").toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
//        Update the adapter with the filtered data
        ListAdapter filteredAdapter = new SimpleAdapter(
                getApplicationContext(), filteredUserList,
                R.layout.list_view_users, new String[]{"user_id", "email", "name", "is_staff"}, new int[]{R.id.uUid, R.id.uEmail, R.id.uName, R.id.uIsStaff}
        );
        listViewUser.setAdapter(filteredAdapter);
    }

//   --------------- General Functions for option menu navigation for Action Bar --------------
//    Create Option Menu in Action Bar
    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the option menu and display the option items when clicked
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
//        Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};
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
        Intent intent = new Intent(UserManagement.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}