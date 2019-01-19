package com.schentrupsoftware.mard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.schentrupsoftware.mard.Fragments.HomeFragment;
import com.schentrupsoftware.mard.Fragments.ListFragment;
import com.schentrupsoftware.mard.Fragments.ScanFragment;
import com.schentrupsoftware.mard.Objects.TagUpdate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_POS = 0;
    private static final int SCAN_POS = 1;
    private static final int LIST_POS = 2;

    private NoSwipePager viewPager;
    private BottomBarAdapter pagerAdapter;
    private FirebaseFirestore db;
    private CurrentLocation currentLocation;

    private EditText tagIDField;
    private EditText speciesField;
    private Spinner sexField;
    private Spinner colorField;

    private ArrayList<TagUpdate> tagUpdateQueue;

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
        currentLocation = new CurrentLocation();
        tagUpdateQueue = new ArrayList<TagUpdate>();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setupViewPager();
        setupDB();
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
        sexField.getSelectedItem().toString();
        colorField.getSelectedItem().toString();
        speciesField.getText().toString();
    }

    private void setupVeiws() {
        View v = viewPager.getFocusedChild();

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

}
