package com.example.kai.philologicum;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kai.philologicum.camera.CameraSource;
import com.example.kai.philologicum.camera.CameraSourcePreview;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

/**
 * Class MainActivity contains the logic for all UI elements, fragments including logic for
 * switching fragments and recognizing text by using OCR-technology. Keep in mind that the logic
 * for single fragments is within each single fragment. Only the logic for LMU Spot can be found
 * here. Tribute to Google inc. for providing the "Mobile Vision API"!
 *
 * @author Kai Barth
 * @author Google Inc.
 * @version 1.5 - 21.12.2018
 * 
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Philologicum";

    //Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    //Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    //camera and preview in order to get a visual feedback
    private CameraSource cameraSource;
    private CameraSourcePreview preview;

    //Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;

    //Fragements
    private FrameLayout content;
    private NewsFragment newsFragment;
    private PlatzfinderFragment platzfinderFragment;
    private PhilologicumFragment philologicumFragment;
    private EinstellungenFragment einstellungenFragment;

    //Bottom Navigation View
    private BottomNavigationView navbar;

    //ImageView for the logo
    private ImageView logo;

    //Link between keywords and location
    private LocationMap locationMap;

    //Check if the app was started first
    private Boolean firstStart = true;

    //if missing the app would call the details activity more then one time because of the fast
    //scan processing on newer devices
    private Boolean lock = false;

    /**
     * Method onCreate contains all UI elements including all fragments
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set good defaults for capturing text.
        final boolean autoFocus = true;
        final boolean useFlash = false;

        //Check for the camera permission before accessing the camera.  If the
        //permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        preview = (CameraSourcePreview) findViewById(R.id.preview);

        //frame for the fragments
        content = findViewById(R.id.content);

        //initialize fragments
        newsFragment = new NewsFragment();
        platzfinderFragment = new PlatzfinderFragment();
        philologicumFragment = new PhilologicumFragment();
        einstellungenFragment = new EinstellungenFragment();

        //Logo
        logo = findViewById(R.id.imageLogo);

        //Navigation-Bar
        navbar = (BottomNavigationView) findViewById(R.id.navigation);
        
        //Initialize the location map containing the links between keywords and locations 
        if (firstStart) {
            locationMap = new LocationMap();
            if (locationMap.buildLocationMap()) {
                Toast.makeText(this, R.string.init_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.init_fail, Toast.LENGTH_SHORT).show();
            }
        }

        //Listen for user interaction
        navbar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navbar_news:
                        content.setVisibility(View.VISIBLE);
                        preview.setVisibility(View.GONE);
                        if (cameraSource != null) {
                            preview.stop();
                            cameraSource.stop();
                        }
                        setFragment(newsFragment);
                        return true;
                    case R.id.navbar_lmu_spot:
                        content.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                        if (cameraSource == null) {
                            createCameraSource(autoFocus, useFlash);
                        }
                        startCameraSource();
                        return true;
                    case R.id.navbar_platzfinder:
                        content.setVisibility(View.VISIBLE);
                        preview.setVisibility(View.GONE);
                        if (cameraSource != null) {
                            preview.stop();
                            cameraSource.stop();
                        }
                        setFragment(platzfinderFragment);
                        return true;
                    case R.id.navbar_philologicum:
                        content.setVisibility(View.VISIBLE);
                        preview.setVisibility(View.GONE);
                        if (cameraSource != null) {
                            preview.stop();
                            cameraSource.stop();
                        }
                        setFragment(philologicumFragment);
                        return true;
                    case R.id.navbar_einstellungen:
                        content.setVisibility(View.VISIBLE);
                        preview.setVisibility(View.GONE);
                        if (cameraSource != null) {
                            preview.stop();
                            cameraSource.stop();
                        }
                        setFragment(einstellungenFragment);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Method setFragment is used for replacing the fragments within the content layout
     */
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }


    /**
     * Handles requestCameraPermission is used for requesting camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then sending the request.
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

    }

    /**
     * Method onTouchEvent contains the logic for user interaction while the camera is active
     * @param e MotionEvent for detecting motion
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);
        return b || super.onTouchEvent(e);
    }

    /**
     * Methode createCameraSource creates and starts the camera.  Note that this uses a higher
     * resolution in comparison to other detection examples to enable the ocr detector to detect
     * small text samples at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        //A text recognizer is created to find text.  An associated multi-processor instance
        //is set to receive the text recognition results, track the text, and maintain
        //graphics for each text block on screen.  The factory is used by the multi-processor to
        //create a separate tracker instance for each text block.
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

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
            }

            //work on line level - each scan triggers a new receiveDetection
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                //save scan to a Sparse Array
                SparseArray<TextBlock> items = detections.getDetectedItems();

                /**
                 * prevent the app from triggering the same DetailsActivity multiple times,
                 * when the MainActivity resumes after a successful recognized text pattern
                 * consequently a successful matched locationId the flag will be true so it deletes
                 * the last detection because this one was already recognized.
                 * Without the flag the DetailsActivity would be called multiple times with the same
                 * recognized locationId
                 **/
                if (lock) {
                    items.clear();
                    lock = false;
                }

                if (items.size() != 0) {
                    //recognized text
                    //iterate through each recognized textblock
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);
                        //iterate through the single textblock
                        if (item != null && item.getValue() != null) {
                            //iterate through each line in a textblock
                            for (Text line : item.getComponents()) {
                                //check if the line is a keyword for a locationID
                                if (locationMap.findLocationId(line.getValue()) != null && !lock) {
                                    //lock that the main activity to only call the DetailsActivity once
                                    lock = true;
                                    //create intent for the new activity
                                    Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
                                    //pass the retrieved locationId
                                    intent.putExtra("locationId", locationMap.findLocationId(line.getValue()));
                                    startActivity(intent);
                                }
                            }
                        }
                    }

                }
            }
        });

        if (!textRecognizer.isOperational()) {
            //Note: The first time that an app using a Vision API is installed on a
            //device, GMS will download a native libraries to the device in order to do detection.
            //Usually this completes before the app is run for the first time.  But if that
            //download has not yet completed, then the above call will not detect any text,
            //barcodes, or faces.
            //
            //isOperational() can be used to check if the required native libraries are currently
            //available.  The detectors will automatically become operational once the library
            //downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            //Check for low storage.  If there is low storage, the native library will not be
            //downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        //Creates and starts the camera.  Note that this uses a higher resolution in comparison
        //to other detection examples to enable the text recognizer to detect small pieces of text.
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
     * Method onStart contains logic for starting the app.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Method onResume starts the camera, that it is ready to be used by other parts of the app
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Method onPause stops the preview and the camera for saving resources
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
        if (cameraSource != null) {
            cameraSource.stop();
        }

    }

    /**
     * Method onDestroy releases the resources associated with the camera source, the associated
     * detectors, and the rest of the processing pipeline.
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
     *
     * Note: It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
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
            //TODO
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
        builder.setTitle("Philologicum")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Method startCameraSource starts or restarts the camera source, if it exists.  If the camera
     * source doesn't exist yet (e.g., because onResume was called before the camera source was
     * created), this will be called again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        //check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
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
         *
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
