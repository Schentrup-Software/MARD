package com.schentrupsoftware.mard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.MenuItem;
import android.widget.TextView;

import com.schentrupsoftware.mard.Fragments.HomeFragment;
import com.schentrupsoftware.mard.Fragments.ListFragment;
import com.schentrupsoftware.mard.Fragments.ScanFragment;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_POS = 0;
    private static final int SCAN_POS = 1;
    private static final int LIST_POS = 2;

    private NoSwipePager viewPager;
    private BottomBarAdapter pagerAdapter;

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
        setupViewPager();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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

}
