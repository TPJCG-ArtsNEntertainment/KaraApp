package mdad.networkdata.karaapp;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Lyrics extends Fragment {
//    Initialization parameters for fragment
    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
    private String mParam1, mParam2;
//    Empty public constructor for fragment
    public Lyrics(){};
//    Create new instance of fragment with this method with parameters
    public static Lyrics newInstance(String param1, String param2) {
        Lyrics lyrics = new Lyrics();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        lyrics.setArguments(args);
        return lyrics;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_lyrics, container, false);
    }

//    ------ Beginning of fragment customization ------
//    Declaration of components from Lyrics's xml
    private WebView webView;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        WebView to display lyrics page
        webView = view.findViewById(R.id.webView);
//        Set webView hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
//        Load url in clicked inside this webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Load the clicked URL in the WebView itself
                view.loadUrl(url);
                return true;
            }
        });
//        Prevent Url fixed in same page
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("https://www.lyrical-nonsense.com/global/");
        }
    }

//  Save webview state prevent crash occur lost of webview state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
}