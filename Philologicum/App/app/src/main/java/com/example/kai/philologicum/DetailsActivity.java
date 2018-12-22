package com.example.kai.philologicum;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for providing the details screen. Up to now its main task is to show additional 
 * information for the LMU-Spot tab.
 * The workflow for the activity is to resolve a passed location-id from the main activity by using
 * the middleware in order to get the connected content-id. Once this is determined the retrieved
 * content-id is passed to the backend in order to get back the information which should be
 * displayed in the activity.
 *
 * Up to now all the information are displayed by using web views. Consequently the displayed
 * information are just websites shrinked to the viewport of the device. It can be manipulated
 * by JavaScript Injections used in the fragments. If the style for example of text color and of
 * the text size should be flexible it would be better to parse the raw data and fetch it to
 * seperate text views. This would make it easier to manipulate the appearance. However, this
 * depends on the future design of the api to the backend.
 */
public class DetailsActivity extends AppCompatActivity {

    //setting up all needed variables
    private Integer locationId;
    private Integer contentId;
    //parts of the middleware url
    private String middlewareBaseUrl = "https://lmulinker.kai-barth.de/php/api.php?";
    private String formatUrl = "format=json";
    private String idTypeUrl = "id_type=location_id";
    private String idUrl = "id=";
    //base url for the backend-api
    private String contentBaseUrl = "https://lmulinker.kai-barth.de/backend/";
    //Elements of the UI
    private WebView webView;
    private ProgressBar loadingPanel;

    /**
     *
     * @param savedInstanceState Bundle which contains the passed location-id
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //retrieve locationId passed by the MainActivity
        Bundle extras = getIntent().getExtras();
        locationId = extras.getInt("locationId");

        //initialize loading panel
        loadingPanel = findViewById(R.id.loadingPanel);

        //initialize webview
        webView = findViewById(R.id.webView);
        //hide webview until data is loaded
        webView.setVisibility(View.GONE);

        //txtMsg1.setText("Passed locationId: " + locationId.toString());
        //txtMsg2.setText("Requested URL: " + middlewareBaseUrl + formatUrl + "&" + idTypeUrl + "&" + idUrl + locationId.toString());

        //prepare the request by setting up a new request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        //Resolve passed locationId
        //create a new JSON-Object
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, middlewareBaseUrl + formatUrl + "&" + idTypeUrl + "&" + idUrl + locationId.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //parse contentId
                            contentId = Integer.parseInt(response.getString("linked_id"));
                            //combine the URL which should be loaded
                            contentBaseUrl = contentBaseUrl + contentId.toString() + ".html";
                            //load the website
                            webView.loadUrl(contentBaseUrl);
                        } catch (JSONException e) {
                            // TODO Handle error
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);

        //Retrieve Content
        //set up a WebViewClient for manipulation the loaded website
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //hide loading panel
                loadingPanel.setVisibility(View.GONE);
                //show the loaded and trimmed webpage
                webView.setVisibility(View.VISIBLE);
            }
        });
    }
}
