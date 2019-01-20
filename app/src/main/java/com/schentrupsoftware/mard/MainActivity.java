package com.schentrupsoftware.mard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.schentrupsoftware.mard.Fragments.HomeFragment;
import com.schentrupsoftware.mard.Fragments.ListFragment;
import com.schentrupsoftware.mard.Fragments.ScanFragment;
import com.schentrupsoftware.mard.Objects.TagUpdate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_POS = 0;
    private static final int SCAN_POS = 1;
    private static final int LIST_POS = 2;
    private static final int requestPermissionID = 101;
    private static final String TAG = "MainActivity";

    private NoSwipePager viewPager;
    private BottomBarAdapter pagerAdapter;
    private FirebaseFirestore db;
    private CurrentLocation currentLocation;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView textView;

    private EditText tagIDField;
    private EditText speciesField;
    private Spinner sexField;
    private Spinner colorField;

    private ArrayList<TagUpdate> tagUpdateQueue;
    private boolean cameraInit = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(HOME_POS);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(SCAN_POS);
                    if (!cameraInit) startCameraSource();
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(LIST_POS);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tagUpdateQueue = new ArrayList<TagUpdate>();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setupLocation();
        setupViewPager();
        setupDB();
    }

    private void setupLocation() {
        currentLocation = new CurrentLocation();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 3, currentLocation);
    }

    private void setupViewPager() {
        viewPager = (NoSwipePager) findViewById(R.id.viewpager);
        viewPager.setPagingEnabled(false);
        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        pagerAdapter.addFragments(new HomeFragment());
        pagerAdapter.addFragments(new ScanFragment());
        pagerAdapter.addFragments(new ListFragment());

        viewPager.setAdapter(pagerAdapter);
    }

    private void setupDB() {
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public void onClickSubmit(View v) {
        setupVeiws();
        TagUpdate tagUpdate = new TagUpdate(
                tagIDField.getText().toString(),
                sexField.getSelectedItem().toString(),
                colorField.getSelectedItem().toString(),
                speciesField.getText().toString(),
                currentLocation);
        insertNewTag(tagUpdate);

        tagIDField.getText().clear();
        sexField.setSelection(0);
        colorField.setSelection(0);
        speciesField.getText().clear();
    }

    private void setupVeiws() {
        View v = viewPager.getChildAt(HOME_POS);

        tagIDField = (EditText) v.findViewById(R.id.numberField);
        speciesField = (EditText) v.findViewById(R.id.speciesField);
        sexField = (Spinner) v.findViewById(R.id.sexDropdown);
        colorField = (Spinner) v.findViewById(R.id.colorDropdown);
    }

    private void insertNewTag(TagUpdate tagUpdate) {
        tagUpdateQueue.add(tagUpdate);
        db.collection("Tag_Distribution")
                .add(tagUpdateQueue.remove(0))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Tag Added", Toast.LENGTH_SHORT);
                        if(!tagUpdateQueue.isEmpty()) {
                            insertNewTag(tagUpdateQueue.remove(0));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Tag add failed. Added to Queue.", Toast.LENGTH_SHORT);
                        System.out.print(e);
                    }
        });
    }
    private void startCameraSource() {

        cameraInit = true;
        View v = viewPager.getChildAt(SCAN_POS);
        cameraView = v.findViewById(R.id.surfaceView);
        textView = v.findViewById(R.id.pictureText);

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            try {
                cameraSource.start(cameraView.getHolder());
            } catch (Exception e){
                Log.e("Error", e.toString());
            }

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){

                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }
}
