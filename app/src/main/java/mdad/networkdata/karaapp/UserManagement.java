package mdad.networkdata.karaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManagement extends AppCompatActivity {
    private ListView listViewUser;
    private SearchView userSearchView;
    private ArrayList<HashMap<String, String>> userList, originalUserList, filteredUserList, targetList;
    private Button  btnCreateUser;
    private String uid,is_staff,username;
    private Boolean is_staffBoolean;
    private String selectedUserId, selectedUserEmail, selectedUserIsStaff;
    private static String url_get_all_users = MainMenu.ipBaseAddress+"get_all_userVolley";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        is_staff = intent.getStringExtra("is_staff");
        username = intent.getStringExtra("username");
        is_staffBoolean = is_staff.equals("1");

        listViewUser = (ListView) findViewById(R.id.listViewUser);
        registerForContextMenu(listViewUser);
        userList = new ArrayList<HashMap<String, String>>();
        postData(url_get_all_users, null);

        userSearchView = findViewById(R.id.userSearchView);
        ListAdapter originalAdapter = new SimpleAdapter(
                UserManagement.this, userList,
                R.layout.list_view_users, new String[]{"uid","email", "name", "is_staff"},
                new int[]{R.id.uUid, R.id.uEmail, R.id.uName, R.id.uIsStaff}
        );
        originalUserList = userList;
        filteredUserList = new ArrayList<>(originalUserList);
        listViewUser.setAdapter(originalAdapter);
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

        btnCreateUser = (Button) findViewById(R.id.btnCreateUser);
        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserManagement.this, AddMusic.class);
                intent.putExtra("uid", uid);
                intent.putExtra("is_staff", is_staff);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        UserManagement.this.getMenuInflater().inflate(R.menu.menu_user, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        HashMap<String, String> rowData = userList.get(info.position);
        Boolean is_staff_currently = rowData.get("is_staff").equals("1");

        if (is_staff_currently){
            menu.findItem(R.id.option_set_user).setVisible(true);
            menu.findItem(R.id.option_set_staff).setVisible(false);
        } else {
            menu.findItem(R.id.option_set_user).setVisible(false);
            menu.findItem(R.id.option_set_staff).setVisible(true);
        }
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (filteredUserList.isEmpty()) targetList = userList;
        else targetList = filteredUserList;
        HashMap<String, String> rowData = targetList.get(info.position);
        selectedUserId = rowData.get("uid");
        selectedUserEmail = rowData.get("email");
        selectedUserPassword = rowData.get("password");
        selectedUserName = rowData.get("name");
        selectedUserIsStaff = rowData.get("is_staff");
        int itemId = item.getItemId();
        if (itemId == R.id.option_set_staff) {
        } else if (itemId == R.id.option_set_user) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicId);
            params_update.put("music_name", selectedMusicName);
            params_update.put("artist_name", selectedMusicArtist);
            params_update.put("url", selectedMusicUrl);
            params_update.put("created_by", selectedMusicCreatedBy);
            params_update.put("is_played", "1");
            postData(url_update_music, params_update, update_delete_music);
        } else if (itemId == R.id.option_edit) {
            Intent intent = new Intent(requireActivity(), EditMusic.class);
            intent.putExtra("uid", MainMenu.uid);
            intent.putExtra("is_staff", MainMenu.is_staff);
            intent.putExtra("username", MainMenu.username);
            intent.putExtra("mid", selectedMusicId);
            startActivity(intent);
        } else if (itemId == R.id.option_remove) {
            Map<String, String> params_update = new HashMap<String, String>();
            params_update.put("mid", selectedMusicId);
            postData(url_delete_music, params_update, update_delete_music);
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
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
            menu.findItem(R.id.item2).setVisible(false);
        }
        return true;
    }
    @Override
    //when the option item is selected
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Array of menu items with their corresponding destination classes
        int[] menuItems = {R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};
        Class<?>[] destinationClasses = {MainMenu.class, UserManagement.class, ProfileSettings.class, RulesAndRegulations.class, Login.class};
        // Iterate over menu items and check conditions
        for (int i = 0; i < menuItems.length; i++) {
            if (id == menuItems[i]) {
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
        Intent intent = new Intent(UserManagement.this, cls);
        intent.putExtra("uid", uid);
        intent.putExtra("is_staff", is_staff);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}