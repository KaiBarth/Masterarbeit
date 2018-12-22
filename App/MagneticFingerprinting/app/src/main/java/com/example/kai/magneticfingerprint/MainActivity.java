package com.example.kai.magneticfingerprint;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class MainActivity contains UI and logic for the MagneticFingerprint app
 * Main goal is to collect a fingerprint of its magnetic environment, show it in the UI and save it
 * in a logfile in order to compare them later on.
 * If Magnetic fingerprinting will be used for the indoor localisation than this app would be the
 * basement from which more components could be implemented.
 *
 * @author Kai Barth
 * @version 1.4 - 21.12.2018
 *
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //initialize Button and TextView for the UI
    private Button btnStartStop;
    private TextView txtOutMagnitude;
    private TextView txtOutX;
    private TextView txtOutY;
    private TextView txtOutZ;
    private Switch logSwitch;
    //flag for starting / stopping the fingerprinting
    private boolean start = false;
    //count number of scans
    private Integer countScans = 0;
    //sensor manager
    private SensorManager sensorManager;
    private FileWriter fileWriter;
    private File file;
    //name of the logfile can be changed here
    private String filename = "MagneticFingerprint.txt";

    /**
     * Method onCreate creates different objects and partly links them to the user interface.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create button and TextViews in the UI
        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        txtOutMagnitude = (TextView) findViewById(R.id.txtOutMagnitude);
        txtOutX = (TextView) findViewById(R.id.txtOutX);
        txtOutY = (TextView) findViewById(R.id.txtOutY);
        txtOutZ = (TextView) findViewById(R.id.txtOutZ);
        logSwitch = (Switch) findViewById(R.id.logSwitch);

        //create SensorManager for collecting magnetic fingerprints
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //initialize file and check if it exists
        file = new File(getBaseContext().getExternalFilesDir(null), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //initialize filewriter in order to save logfiles from one session
        //if logfiles should be collected from more than one session use the append-mode
        //append-mode:
        // fileWriter = new FileWriter(file, true);
        try {
            fileWriter = new FileWriter(file);
            //if you want to create a CSV-Logfile use the following line as a header
            fileWriter.write("Fingerprint; Model; Time; X; Y; Z; Magnitude" + System.getProperty("line.separator"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Method btnStartStop contains the onClickListener and logic to start and stop the
     * fingerprinting mechanism.
     * @param view
     */
    public void btnStartStop (View view) {
        if (!start) {
            start = true;
            btnStartStop.setText("Stop");
            Toast.makeText(MainActivity.this, "Collecting Fingerprints started", Toast.LENGTH_SHORT).show();
        } else {
            start = false;
            btnStartStop.setText("Start");
            Toast.makeText(MainActivity.this, countScans + " Fingerprints collected", Toast.LENGTH_SHORT).show();
            //reset counter
            countScans = 0;
        }
    }

    /**
     * Method onResume contains the logic when the app comes back to the foreground
     * register the sensor manager for continuing the fingerprinting mechanism.
     */
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Method onPause contains the logic when the app is paused and goes to the background
     * Stop the fingerprinting mechanism by unregistering the sensor manager.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Method onSensorChanged contains the logic for fingerprinting. It is listening to sensor
     * changes, catches them and shows them in the UI.
     * @param event containing the magnetic fingerprint
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (start) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //increase counter
                countScans++;
                // get values for each axes X,Y,Z
                float magX = event.values[0];
                float magY = event.values[1];
                float magZ = event.values[2];
                //calculate magnitude
                double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));
                // show values in the UI
                txtOutX.setText("X: " + String.valueOf(magX));
                txtOutY.setText("Y: " + String.valueOf(magY));
                txtOutZ.setText("Z: " + String.valueOf(magZ));
                txtOutMagnitude.setText("Magnitude: " + String.valueOf(magnitude));
                // write the fingerprint to the logfile
                if (logSwitch.isChecked()) {
                    writeToLog(magX, magY, magZ, magnitude);
                }
                //activate if you want to collect a certain amount of fingerprints!
                /*
                if (countScans == 100) {
                    btnStartStop(null);
                }
                */

            }
        }
    }

    /**
     * Method onAccuracyChanged is needed for the method onSensorChanged
     * @param accuracy
     * @param sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Method writeToLog writes the collected fingerprint to a logfile on the internal drive.
     * At the moment this is the directory of the app on the internal space
     * @param magX Float contains the value for X as float
     * @param magY Float contains the value for Y as float
     * @param magZ Float contains the value for Z as float
     * @param magnitude Double contains the value for magnitude as double
     */
    public void writeToLog(float magX, float magY, float magZ, double magnitude) {
        try {
            //write a header of the fingerprint with scan number, timestamp and device model number
            /* Use the following lines if you want a non CSV-Logfile which is easily human-readable
            fileWriter.write("Fingerprint " + countScans + System.getProperty("line.separator"));
            fileWriter.write("Model: " + Build.MODEL + System.getProperty("line.separator"));
            fileWriter.write("Time: " + getTimeStamp() + System.getProperty("line.separator"));
            fileWriter.write("X: " + magX + System.getProperty("line.separator"));
            fileWriter.write("Y: " + magY + System.getProperty("line.separator"));
            fileWriter.write("Z: " + magZ + System.getProperty("line.separator"));
            fileWriter.write("Magnitude: " + magnitude + System.getProperty("line.separator"));
            fileWriter.write(System.getProperty("line.separator"));
            */
            // Use the following line if you want a CSV-Logfile - Don't forget the header on top!
            fileWriter.write(countScans + "; " + Build.MODEL + "; " + getTimeStamp() + "; " + magX + "; " + magY + "; " + magZ + "; " + magnitude + System.getProperty("line.separator"));
            //let the file open for more information - system is closing it on destroy
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
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
}
