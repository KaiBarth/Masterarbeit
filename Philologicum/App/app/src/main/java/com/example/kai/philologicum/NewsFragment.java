package com.example.kai.philologicum;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


/**
 * A simple {@link Fragment} subclass, containing the logic for the news.
 */
public class NewsFragment extends Fragment {

    private WebView webView;

    //which website should be parsed?
    private String parseUrl = "https://www.ub.uni-muenchen.de/index.html";
    private ProgressBar loadingPanel;

    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_news, container, false);

        //initialize loading panel
        loadingPanel = fragmentView.findViewById(R.id.loadingPanel);

        //initialize webview
        webView = (WebView) fragmentView.findViewById(R.id.webView);
        //hide webview, otherwise the user would see the parts of the website which should be trimmed
        webView.setVisibility(View.GONE);
        //set up a WebViewClient for manipulation the loaded website
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //trim loaded webview to the main content by inject javascript
                view.loadUrl("javascript:(function() { " +
                        "document.getElementById('search').style.display='none'; "+
                        "document.getElementById('homeMobile').style.display='none'; "+
                        "document.getElementById('barMobile').style.display='none'; "+
                        "document.getElementById('bc').style.display='none'; "+
                        "document.getElementById('service').style.display='none'; "+
                        "document.getElementById('footer').style.display='none'; "+
                        "})()");
                //hide loading panel
                loadingPanel.setVisibility(View.GONE);
                //show the loaded and trimmed webpage
                webView.setVisibility(View.VISIBLE);
            }
        });
        //enable javascript
        webView.getSettings().setJavaScriptEnabled(true);
        //load the website
        webView.loadUrl(parseUrl);
        //return the fragment to the main activity
        return fragmentView;
    }

}
