/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 *
 * @author Google Inc.
 * @author Kai Barth
 * @version 1.2 21.12.2018
 */
public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    //camera and graphical overlay
    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;

    //initializing file
    private FileWriter fileWriter;
    private File file;
    private String filename = "OCR.txt";
    //renaming the files
    private EditText renameLogfile;
    private AlertDialog.Builder alertDialog;

    //count scans
    private Integer countScans = 0;

    //variables for recognizing a line twice and for checking the first run
    private boolean isStageOnePassed = false;
    private boolean isFirstRun = true;

    //Saving recognized lines from the last scan
    private ArrayList<String> tempCompare = new ArrayList<String>();

    //saving the keywords for the locationID's
    private HashMap<String, Integer> locationMap = new HashMap<String, Integer>();
    private Integer locationID = null;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ocr_capture);

        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        //rename the file
        alertDialog = new AlertDialog.Builder(OcrCaptureActivity.this);
        renameLogfile = new EditText(OcrCaptureActivity.this);


        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = false;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(graphicOverlay, "Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();


        //initialize file and check if it exists
        file = new File(getBaseContext().getExternalFilesDir(null), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //save the head of the logfile
        try {
            fileWriter = new FileWriter(file);
            //if you want to create a CSV-Logfile use the following line
            fileWriter.write("scan; model; timestamp; text; twice recognized; locationID" + System.getProperty("line.separator"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //initialize hashmap containing keywords for locationID's - different keywords can point to the same location
        locationMap.clear();

        //locationID's for testscenario panel 1 front
        locationMap.put("Miss Broke had",11);
        locationMap.put("that kind of beauty which",11);
        locationMap.put("seems to be thrown",11);
        locationMap.put("into relief by poor dress.",11);
        locationMap.put("Who's",11);
        locationMap.put("there?",11);
        locationMap.put("Hohe Herren",11);
        locationMap.put("von der Akademie!",11);
        locationMap.put("ÄNTLIGEN STOD",11);
        locationMap.put("PRÄSTEN I",11);
        locationMap.put("PREDIKSTOLEN.",11);
        locationMap.put("Il faut, autant qu’on peut, obliger tout le monde:",11);
        locationMap.put("On a souvent besoin d’un plus petit que soi.",11);
        locationMap.put("Lydia brukade",11);
        locationMap.put("bada ensam",11);

        //locationID's for testscenario panel 1 back
        locationMap.put("Hemos perdido",12);
        locationMap.put("aun este",12);
        locationMap.put("crepúsculo.",12);
        locationMap.put("De o săptămînă …",12);
        locationMap.put("De o săptămînă nu mai am linişte,",12);
        locationMap.put("fir-ar să fie!",12);
        locationMap.put("Veliká doba",12);
        locationMap.put("žádá velké l idi.",12);
        locationMap.put("Nay answer me:",12);
        locationMap.put("Stand & vnfold your selfe.",12);
        locationMap.put("Erschrecken Sie nicht, meine Freundin,",12);
        locationMap.put("anstatt der Handschrift von Ihrer Sternheim",12);
        locationMap.put("eine gedruckte Copey zu erhalten,",12);
        locationMap.put("welche Ihnen auf einmal die ganze Verräterei",12);
        locationMap.put("entdeckt, die ich an Ihnen begangen habe.",12);
        locationMap.put("Umana cosa è",12);
        locationMap.put("aver compassione",12);
        locationMap.put("degli aff litti",12);
        locationMap.put("Egy kissé mindjárt",12);
        locationMap.put("hátra is hoköltem tolük,",12);
        locationMap.put("természetesen.",12);

        ////locationID's for testscenario panel 2 front
        locationMap.put("GALLIA EST OMNIS DIVISA IN PARTES TRES,",21);
        locationMap.put("QUARUM UNAM INCOLUNT BELGAE,",21);
        locationMap.put("ALIAM AQUITANI, TERTIAM QUI IPSORUM LINGUA CELTAE,",21);
        locationMap.put("NOSTRA GALLI APPELANTUR.",21);
        locationMap.put("Мой дядя самых честных правил,",21);
        locationMap.put("Когда не в шутку занемог,",21);
        locationMap.put("Он уважать себя заставил.",21);
        locationMap.put("И лучше выдумать не мог.",21);
        locationMap.put("Legalább vállfát",21);
        locationMap.put("ne kelljen keresgélniük.",21);

        ////locationID's for testscenario panel 2 back
        locationMap.put("Ἄνδρα μοι ἔννεπε, Μοῦσα, πολύτροπον, ὃς μάλα πολλὰ",22);
        locationMap.put("πλάγχϑη, ἐπεὶ Τροίης ἱερὸν πτολίεϑρον ἔπερσε·",22);
        locationMap.put("πολλῶν δ' ἀνϑρώπων ἴδεν ἄστεα καὶ νόον ἔγνω,",22);
        locationMap.put("πολλὰ δ' ὅ γ' ἐν πόντῳ πάϑεν ἄλγεα ὃν κατὰ ϑυμόν,",22);
        locationMap.put("ἀρνύμενος ἥν τε ψυχὴν καὶ νόστον ἑταίρων.",22);
        locationMap.put("Mieleni minun tekevi,",22);
        locationMap.put("Aivoni ajattelevi,",22);
        locationMap.put("Lähteäni laulamahan,",22);
        locationMap.put("Saa’ani sanelemahan,",22);
        locationMap.put("Sukuvirttä suoltamahan,",22);
        locationMap.put("Lajivirttä laulamahan;",22);

        ////locationID's for testscenario panel 3 front
        locationMap.put("En un lugar de la Mancha,",31);
        locationMap.put("de cuyo nombre no quiero acordarme ...",31);
        locationMap.put("Hljóðs bið ek allar",31);
        locationMap.put("helgar kindir,",31);
        locationMap.put("meiri ok minni",31);
        locationMap.put("mögu Heimdallar;",31);
        locationMap.put("viltu, at ek, Valföðr!",31);
        locationMap.put("vel framtelja",31);
        locationMap.put("forn spjöll fíra,",31);
        locationMap.put("þau er fremst um man.",31);
        locationMap.put("Ποικιλόθρον᾽ ὰθάνατ᾽ ᾽Αφροδιτα,",31);
        locationMap.put("παῖ Δίοσ, δολόπλοκε, λίσσομαί σε",31);
        locationMap.put("μή μ᾽ ἄσαισι μήτ᾽ ὀνίαισι δάμνα,",31);
        locationMap.put("πότνια, θῦμον.",31);

        ////locationID's for testscenario panel 3 back
        locationMap.put("It was a queer. sultry summer,",32);
        locationMap.put("the summer they electrocuted",32);
        locationMap.put("the Rosenbergs, and I didn’t know",32);
        locationMap.put("Sein Blick ist vom Vorübergehn der Stäbe",32);
        locationMap.put("what I was doing in New York.",32);
        locationMap.put("so müd geworden, daß er nichts mehr hält.",32);
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);


        return b || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        /**
         * Called by the detector to deliver detection results.
         * If your application called for it, this could be a place to check for
         * equivalent detections by tracking TextBlocks that are similar in location and content from
         * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
         * multiple detections.
         */
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
                graphicOverlay.clear();
            }

            //work on line level - each scan triggers a new receiveDetection
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                //increase scan
                countScans++;
                graphicOverlay.clear();

                //save scan to a Sparse Array
                SparseArray<TextBlock> items = detections.getDetectedItems();

                //no recognized text in the received scans
                if (items.size() == 0) {
                    //reset temp Array List
                    tempCompare.clear();
                    //write logfile
                    writeToLog("no Value detected");
                } else {
                    //recognized text
                    //iterate through each recognized textblock
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);

                        //iterate through the single textblock
                        if (item != null && item.getValue() != null) {
                            //iterate through each line in a textblock
                            for (Text line : item.getComponents()) {
                                //compare each line to the previous ones

                                //check if the line is a keyword for a locationID
                                if(locationMap.containsKey(line.getValue())) {
                                    //save locationID for the logfile
                                    locationID = locationMap.get(line.getValue());
                                }

                                if (!isFirstRun && tempCompare.contains(line.getValue())) {
                                    //line is recognized twice
                                    isStageOnePassed = true;
                                } else if (isFirstRun) {
                                    //save all recognized lines to the tempArray
                                    tempCompare.add(line.getValue());
                                }

                                //write logfile
                                writeToLog(line.getValue());
                                //reset twice recognition and matched locationID
                                isStageOnePassed = false;
                                locationID = null;
                            }

                            //render textblock
                            OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                            graphicOverlay.add(graphic);
                        }
                    }

                    if (!isFirstRun) {
                        //clear array list and build it new for the next comparison
                        tempCompare.clear();
                        for (int i = 0; i < items.size(); ++i) {
                            TextBlock item = items.valueAt(i);
                            if (item != null && item.getValue() != null) {
                                for (Text line : item.getComponents()) {
                                    tempCompare.add(line.getValue());
                                }
                            }
                        }
                    }

                    //change the flag for the first scan
                    if (isFirstRun) {
                        isFirstRun = false;
                    }

                }
            }
        });

            /* work on item level --> use this method
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                countScans++;
                graphicOverlay.clear();
                SparseArray<TextBlock> items = detections.getDetectedItems();
                Log.i("Line: ", "Scan " + countScans);
                if (items.size() == 0) {
                    //reset temp Array List
                    tempCompare.clear();

                    try {
                        fileWriter.write(countScans + "; " + getTimeStamp() + "; " + "no Value detected" + System.getProperty("line.separator"));
                        fileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);
                        if (item != null && item.getValue() != null) {
                            Log.i("Line: ", "item " + item.getValue());
                            //check if value was recognized in a scan before
                            if (!isFirstRun && tempCompare.contains(item.getValue())) {
                                if (isStageOnePassed) {
                                    Snackbar.make(graphicOverlay, item.getValue() + " was found twice!",
                                            Snackbar.LENGTH_LONG)
                                            .show();

                                    isStageOnePassed = false;
                                    tempCompare.clear();
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    continue;
                                }
                                isStageOnePassed = true;
                            } else if (isFirstRun) {
                                tempCompare.add(item.getValue());
                            }

                            OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                            graphicOverlay.add(graphic);

                            try {
                                fileWriter.write(countScans + "; " + getTimeStamp() + "; " + item.getValue() + "; " + item.getLanguage() + "; " + isStageOnePassed + "; " + System.getProperty("line.separator"));
                                fileWriter.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!isFirstRun && !isStageOnePassed) {
                        //no match found reset array and save new values for next comparison
                        tempCompare.clear();
                        for (int i = 0; i < items.size(); ++i) {
                            TextBlock item = items.valueAt(i);
                            if (item != null && item.getValue() != null) {
                                tempCompare.add(item.getValue());
                            }
                        }
                    }


                    if (isFirstRun) {
                        isFirstRun = false;
                    }

                }
            }
        });
        */


        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                        .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, true);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * Method getTimeStamp defines the format of a timestamp and returns it to the calling method
     *
     * @return String Returns the current date and time as a string
     */
    private String getTimeStamp() {
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
     * Method writeToLog writes the collected fingerprint to a logfile on the internal drive.
     * At the moment this is the directory of the app on the internal space
     *
     * @param line String contains the recognized OCR String
     */
    public void writeToLog(String line) {
        try {
            fileWriter.write(countScans + "; " + Build.MODEL + "; " + getTimeStamp() + "; " + line + "; " + isStageOnePassed + "; " + locationID + "; " + System.getProperty("line.separator"));
            //let the file open for more information - system is closing it on destroy
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method onBackPressed overwrites super class in order to add the possibility to change the
     * name of the created logfile
     */
    @Override
    public void onBackPressed() {
        //stop camera
        cameraSource.stop();
        //build alert dialog for renaming the logfile if needed
        alertDialog.setTitle("Rename Logfile?");
        alertDialog.setMessage("Current Logfilename: " + filename);
        //fix crash if dialog is build twice
        if (renameLogfile.getParent() != null) {
            ((ViewGroup) renameLogfile.getParent()).removeView(renameLogfile);
        }
        alertDialog.setView(renameLogfile);
        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCameraSource();
                dialog.cancel();
            }
        });
        alertDialog.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user wants to rename the file
                if (!renameLogfile.getText().toString().isEmpty()) {
                    String newFileName = renameLogfile.getText().toString();
                    //add *.txt if not given by the user
                    if (!newFileName.contains(".txt")) {
                        newFileName = newFileName + ".txt";
                    }
                    File newFile = new File(getBaseContext().getExternalFilesDir(null), newFileName);
                    file.renameTo(newFile);
                }
                OcrCaptureActivity.super.onBackPressed();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (cameraSource != null) {
                cameraSource.doZoom(detector.getScaleFactor());
            }
        }

    }

}
