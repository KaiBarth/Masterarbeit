package com.example.kai.wlanscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class MainActivity contains UI and logic for the WLANFingerprint app
 * Main goal is to collect a fingerprint of the device WLAN environment, show it in the UI
 * and save it in a logfile in order to compare them later on.
 * If WLAN fingerprinting will be used for the indoor localisation than this app would be the
 * basement from which more components could be implemented
 *
 * Note for Android 9 API Level 28: Take care of WLANScan Throttling
 * If you perform more than 4 scans in 2 minutes this error will occur:
 * 2018-09-28 21:18:49.865 1275-4538/? E/WifiService: Failed to start scan
 *
 * @author Kai Barth
 * @version 1.3 - 21.12.2018
 *
 */

public class MainActivity extends AppCompatActivity {

    //setup all class variables and objects
    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private LocationManager locationManager;
    private AlertDialog.Builder alertDialog;
    private Switch logSwitch;
    private File file;
    private FileWriter fileWriter;
    private TextView scanTxt;
    private ProgressBar loadingPanel;
    //name of the logfile can be changed here
    private String filename = "WLANFingerprint.txt";
    //amount of scans triggered in a session
    private Integer countScans = 0;

    /**
     * Method onCreate creates different objects and partly links them to the user interface
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, 22);
        }

        //initialize UI-elements
        buttonScan = findViewById(R.id.scanBtn);
        logSwitch = findViewById(R.id.logSwitch);
        listView = findViewById(R.id.wifiList);
        scanTxt = findViewById(R.id.scanTxt);
        loadingPanel = findViewById(R.id.loadingPanel);

        //set loading panel invisible
        loadingPanel.setVisibility(View.GONE);

        //initialize wifi and location manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        alertDialog = new AlertDialog.Builder(this);

        //set onClickListener to start the WLAN-scan
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });

        //initialize file and check if it exists
        file = new File(getBaseContext().getExternalFilesDir(null), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //initialize filewirter will save logfiles from one session
        //if the logfiles should be collected from more than one session use the append-mode
        //append-mode:
        // fileWriter = new FileWriter(file, true);
        try {
            fileWriter = new FileWriter(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //check if WLAN is activated
        if (!wifiManager.isWifiEnabled()) {
            alertDialog.setTitle("WLAN is disabled!");
            alertDialog.setMessage("In order to catch a WLAN-fingerprint, you have to enable WLAN" + System.getProperty("line.separator") + "Note that this dialog is not cancelable!");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    wifiManager.setWifiEnabled(true);
                }
            });
            alertDialog.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.create();
            alertDialog.show();
        }

        //check if location is activated - but only for API-Level 28
        if (Build.VERSION.SDK_INT == 28 && !locationManager.isLocationEnabled()) {
            alertDialog.setTitle("Location is disabled!");
            alertDialog.setMessage("In order to catch a WLAN-fingerprint, you have to enable the location" + System.getProperty("line.separator") + "Note that this dialog is not cancelable!");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.create();
            alertDialog.show();
        } else {
            Toast.makeText(this, "Make sure you activated the location!", Toast.LENGTH_SHORT).show();
        }

        //connect the adapter and array to the listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

    }

    /**
     * Method scanWifi contains the logic to initiate a wifi scan by registering a receiver.
     */
    private void scanWifi() {
        //show loading page
        loadingPanel.setVisibility(View.VISIBLE);
        //disable button in order to prevent it from being pressed again
        buttonScan.setEnabled(false);
        //increase number of scans
        countScans++;
        scanTxt.setText("Scan " + countScans);
        arrayList.clear();
        //listView.setAdapter(adapter);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    /**
     * Broadcast receiver is part of the class definition. In order to improve the human
     * readability it is written down here
     */
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        /**
         * Method onReceive contains the wifi scan results and saves them in an array list
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for (ScanResult scanResult : results) {
                //for the fingerprint at the moment only SSID and RSSI are collected. This could be extended
                arrayList.add("SSID: " + scanResult.SSID +  " | RSSI: " + Integer.toString(scanResult.level));
                //refresh the adapter to display the results
                adapter.notifyDataSetChanged();
            }

            //check if a logfile should be created
            if (logSwitch.isChecked()) {
                writeToLog();
            }

            //disable the loading screen
            loadingPanel.setVisibility(View.GONE);
            //enable the button
            buttonScan.setEnabled(true);
        }
    };

    /**
     * Method writeToLog writes the collected fingerprint to a logfile on the internal drive.
     * At the moment this is the directory of the app on the internal space
     */
    private void writeToLog() {
        try {
            //write a header of the fingerprint with scan number, timestamp and device model number
            fileWriter.write("Fingerprint " + countScans + System.getProperty("line.separator"));
            fileWriter.write("Model: " + Build.MODEL + System.getProperty("line.separator"));
            fileWriter.write("Time: " + getTimeStamp() + System.getProperty("line.separator"));
            for (String line : arrayList) {
                fileWriter.write(line);
                fileWriter.write(System.getProperty("line.separator"));
            }
            fileWriter.write(System.getProperty("line.separator"));
            //let the file open for more information - system is closing it on destroy
            fileWriter.flush();
            Toast.makeText(this, "Data written to Logfile!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Data NOT written to Logfile!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method getTimeStamp defines the format of a timestamp and returns it to the calling method
     * @return String Returns the current date and time as a string
     */
    private String getTimeStamp () {
        try {
            //defining the timestamp format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //find todays date
            String currentDateTime = dateFormat.format(new Date());
            //return the string to the calling method
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method onRequestPermissionResult overrides the original one in order to check if the
     * user has granted the needed permissions to launch the app correctly
     * @param grantResults
     * @param permissions
     * @param requestCode
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 22:
                {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    return;
                } else {
                    // permission denied,
                    MainActivity.this.finish();
                }
                return;
            }
        }
    }
}
