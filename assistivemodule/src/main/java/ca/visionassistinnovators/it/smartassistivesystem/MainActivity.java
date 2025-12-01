/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Daksh Rana – N01664095
 * Kush Patel – N01657387
 */
package ca.visionassistinnovators.it.smartassistivesystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import ca.visionassistinnovators.it.smartassistivesystem.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final long MAX_SPLASH_TIME = 4000L;

    private volatile boolean isTimeout   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ INSTALL SPLASH SAFELY
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        splashScreen.setOnExitAnimationListener(splashScreenView -> {


            View icon = splashScreenView.getIconView();

            if (icon.getWindowToken() == null) {
                // ✅ Happens during instrumented tests → safely remove
                splashScreenView.remove();
                return;
            }

            // ✅ Safe rotation + fade animation
            icon.animate()
                    .rotationBy(360f)
                    .setDuration(800)
                    .withEndAction(() -> {

                        if (icon.getWindowToken() == null) {
                            splashScreenView.remove();
                            return;
                        }

                        icon.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(splashScreenView::remove)
                                .start();
                    })
                    .start();
        });



        // ✅ SPLASH TIMEOUT SAFETY (4s MAX)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isTimeout = true;
            checkAndProceed();
        }, MAX_SPLASH_TIME);
    }


    // ---------------------------------------------------------
    //  PROCEED WHEN BOTH DONE OR TIMEOUT
    // ---------------------------------------------------------
    private void checkAndProceed() {

        boolean isWriteDone = false;
        if (isTimeout) {
            goLogin();
        }
    }

    // ---------------------------------------------------------
    //  MOVE TO LOGIN
    // ---------------------------------------------------------
    private void goLogin() {

        if (isFinishing()) return;

        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        finish();
    }
}
