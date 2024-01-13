package mdad.networkdata.karaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

public class MusicHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_history);

        WebView webView = findViewById(R.id.webView3);
        String video = "<iframe width=\"99%\" height=\"100%\" src=\"https://www.youtube-nocookie.com/embed/V2KCAfHjySQ?si=VFmk_dDI73qbc0GF\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>";
        webView.loadData(video, "text/html", "utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
    }


    @Override
//add the option menu to the activity
    public boolean onCreateOptionsMenu(Menu menu)
    {
// Inflate the option menu and display the option items when clicked;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
//when the option item is selected
    public boolean onOptionsItemSelected(MenuItem item)
    {
// get the id of the selected option item
        int id = item.getItemId( );
        if (id == R.id.item1) { // MainMenu option is selected
            Toast.makeText(getApplicationContext(),"Main Activity Selected",Toast.LENGTH_LONG).show();
            Intent i = new Intent(MusicHistory.this, MainMenu.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.item2)
        { // SecondActivity option is selected
            Toast.makeText(getApplicationContext(),"Add Music Selected",Toast.LENGTH_LONG).show();
            // navigate to SecondActivity
            Intent i = new Intent(MusicHistory.this, AddMusic.class);
            startActivity(i);
            return true;
        }

        else if (id == R.id.item3)
        {
            Toast.makeText(getApplicationContext(),"Edit Music Selected",Toast.LENGTH_LONG).show();

            Intent i = new Intent(MusicHistory.this, EditMusic.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.item4)
        {
            Toast.makeText(getApplicationContext(),"Music History Selected",Toast.LENGTH_LONG).show();

            return true;
        }
        else if (id == R.id.item5)
        {
            Toast.makeText(getApplicationContext(),"Rules and Regulations Selected",Toast.LENGTH_LONG).show();
            Intent i = new Intent(MusicHistory.this, RulesRegulations.class);
            startActivity(i);

            return true;
        }



        else
            return super.onOptionsItemSelected(item);
    }
}
