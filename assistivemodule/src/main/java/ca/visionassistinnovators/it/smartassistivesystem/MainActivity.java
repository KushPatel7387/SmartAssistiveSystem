/**
 * MainActivity
 * Hosts Navigation Drawer and manages fragments.
 *
 * Team: Vision Assist Innovators
 * Members:
 *  - Sarang Prajapati (N01662036)
 *  - Krish Patel (N01666556)
 *  - Kush Patel (N01657387)
 *  - Daksh Rana (N01664095)
 * Section: [CENG-323 OCA]
 */
package ca.visionassistinnovators.it.smartassistivesystem;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import ca.visionassistinnovators.it.smartassistivesystem.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Toolbar
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Define top-level destinations (fragments in navigation)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();

        // Set up NavController with Drawer
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // ✅ Handle back press using OnBackPressedDispatcher (modern way)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Exit App")
                        .setMessage("Do you really want to exit Smart Assistive System?")
                        .setIcon(R.mipmap.ic_launcher) // replace with custom app icon
                        .setPositiveButton("Yes", (dialog, which) -> {
                            finishAffinity(); // Exit app completely
                        })
                        .setNegativeButton("Stay", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate top-right menu if needed
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
