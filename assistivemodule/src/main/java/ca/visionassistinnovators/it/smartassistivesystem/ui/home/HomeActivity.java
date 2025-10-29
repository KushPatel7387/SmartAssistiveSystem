package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // AppBar + Drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_magnifier,
                R.id.nav_sos,R.id.nav_alerts, R.id.nav_sensors,
                R.id.nav_profile, R.id.nav_about, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // TABS
        tabLayout = findViewById(R.id.tab_layout);
        setupTabs();

        // Sync Tab with Navigation
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) navController.navigate(R.id.nav_home);
                else if (position == 1) navController.navigate(R.id.nav_magnifier);
                else if (position == 2) navController.navigate(R.id.nav_sos);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Sync Navigation with Tab
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_home) {
                tabLayout.selectTab(tabLayout.getTabAt(0));
            } else if (destination.getId() == R.id.nav_magnifier) {
                tabLayout.selectTab(tabLayout.getTabAt(1));
            } else if (destination.getId() == R.id.nav_sos) {
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        });

        // Back Press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(R.string.exit_app2)
                        .setMessage(R.string.do_you_really_want_to_exit_smart_assistive_system2)
                        .setIcon(R.drawable.ic_exit)
                        .setPositiveButton("Yes", (d, w) -> finishAffinity())
                        .setNegativeButton("Stay", (d, w) -> d.dismiss())
                        .show();
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Home").setContentDescription("Home Dashboard"));
        tabLayout.addTab(tabLayout.newTab().setText("Magnifier").setContentDescription("Screen Magnifier"));
        tabLayout.addTab(tabLayout.newTab().setText("SOS").setContentDescription("Emergency Alert"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}