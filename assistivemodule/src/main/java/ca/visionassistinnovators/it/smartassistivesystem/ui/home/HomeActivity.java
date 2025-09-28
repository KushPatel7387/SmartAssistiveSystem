package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ uses your drawer layout

        // ✅ Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Setup Drawer + NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // ✅ Define top-level destinations (updated to match your drawer items)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_alerts,
                R.id.nav_sensors,
                R.id.nav_profile,
                R.id.nav_about,
                R.id.nav_settings // ✅ Settings added
        )
                .setOpenableLayout(drawer)
                .build();

        // ✅ Setup NavController
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        // ✅ Exit confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to exit Smart Assistive System?")
                .setIcon(R.drawable.ic_exit)
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
