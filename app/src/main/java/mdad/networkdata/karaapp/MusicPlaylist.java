package mdad.networkdata.karaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MusicPlaylist extends Fragment
{
    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
    private String mParam1, mParam2;
    public MusicPlaylist(){};
    public static MusicPlaylist newInstance(String param1, String param2)
    {
        MusicPlaylist musicPlaylist = new MusicPlaylist();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        musicPlaylist.setArguments(args);
        return musicPlaylist;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_music_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
    }
}