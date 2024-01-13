package mdad.networkdata.karaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class RulesRegulations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules_regulations);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the option menu and display the option items when clicked;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId( );
        if (id == R.id.item1)
        {
            Toast.makeText(getApplicationContext(),"Main Activity Selected",Toast.LENGTH_LONG).show();
// To navigate to MainMenu
            Intent i = new Intent(RulesRegulations.this, MainMenu.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.item2)
        {
            Toast.makeText(getApplicationContext(),"Add Music Selected",Toast.LENGTH_LONG).show();
            Intent i = new Intent(RulesRegulations.this, AddMusic.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.item3)
        {
            Toast.makeText(getApplicationContext(),"Edit Music Selected",Toast.LENGTH_LONG).show();

            return true;
        }
        else if (id == R.id.item4)
        {
            Toast.makeText(getApplicationContext(),"Music History Selected",Toast.LENGTH_LONG).show();
            Intent i = new Intent(RulesRegulations.this, MusicHistory.class);
            startActivity(i);

            return true;
        }
        else if (id == R.id.item5)
        {
            Toast.makeText(getApplicationContext(),"Rules and Regulations Selected",Toast.LENGTH_LONG).show();


            return true;
        }

        else
            return super.onOptionsItemSelected(item);
    }
}
