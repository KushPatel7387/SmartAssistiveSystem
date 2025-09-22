/**
 * Hosts Navigation Drawer, Splash Screen, Toolbar, and manages fragments.
 *
 * Team: Vision Assist Innovators
 * Members:
 *  - Sarang Prajapati (N01662036)
 *  - Krish Patel (N01666556)
 *  - Kush Patel (N01657387)
 *  - Daksh Rana (N01664095)
 * Section: 3DTues
 */

package ca.visionassistinnovators.it.smartassistivesystem;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = System.currentTimeMillis();

        // ✅ Install SplashScreen API
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // ✅ Keep splash visible for 3 seconds
        splashScreen.setKeepOnScreenCondition(() -> {
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed < 3000; // true = still show splash
        });

        // ✅ Animate splash icon (fade + zoom) when exiting splash
        splashScreen.setOnExitAnimationListener(splashView -> {
            splashView.getIconView().animate()
                    .alpha(0f)
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(1000) // 1 second exit animation
                    .withEndAction(splashView::remove) // remove after animation
                    .start();
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Set Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Setup Drawer + NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // ✅ Define top-level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_about)
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
        // ✅ Exit confirmation dialog with custom icon
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to exit Smart Assistive System?")
                .setIcon(R.drawable.ic_exit) // add ic_exit.png in res/drawable
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
