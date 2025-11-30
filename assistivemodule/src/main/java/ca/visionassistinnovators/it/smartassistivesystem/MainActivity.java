/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ca.visionassistinnovators.it.smartassistivesystem.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long MAX_SPLASH_TIME = 2000L;

    private volatile boolean isWriteDone = false;
    private volatile boolean isReadDone  = false;
    private volatile boolean isTimeout   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 Auto Light/Dark background
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        }

        super.onCreate(savedInstanceState);

        // ---------------------------------------------------------------
        // 🚀 ANDROID 12+ SPLASH SCREEN + ZOOM/FADE ANIMATION
        // ---------------------------------------------------------------
        final SplashScreen splash = SplashScreen.installSplashScreen(this);

        splash.setOnExitAnimationListener(splashScreenView -> {

            View iconView = splashScreenView.getIconView();

            if (iconView != null) {

                // Start slightly small and transparent
                iconView.setScaleX(0.75f);
                iconView.setScaleY(0.75f);
                iconView.setAlpha(0f);

                // Zoom-in + fade-in animation
                iconView.animate()
                        .scaleX(1.15f)       // smooth zoom in
                        .scaleY(1.15f)
                        .alpha(1f)           // fade in
                        .setDuration(500L)
                        .withEndAction(() -> {

                            // Fade out animation
                            iconView.animate()
                                    .alpha(0f)
                                    .setDuration(350L)
                                    .withEndAction(splashScreenView::remove)
                                    .start();
                        })
                        .start();

            } else {
                splashScreenView.remove();
            }
        });

        // Keep splash until Firebase done or timeout
        splash.setKeepOnScreenCondition(() ->
                !(isWriteDone && isReadDone) && !isTimeout
        );

        // Firebase logic (unchanged)
        doFirebaseTestWrite();
        doFirebaseTestRead();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isTimeout = true;
            checkAndProceed();
        }, MAX_SPLASH_TIME);
    }

    private void doFirebaseTestWrite() {
        DatabaseReference root = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference();

        Map<String, Object> testData = new HashMap<>();
        testData.put(getString(R.string.rtdb_field_message), getString(R.string.hello_from_android));
        testData.put(getString(R.string.rtdb_field_timestamp), System.currentTimeMillis());

        root.child(getString(R.string.rtdb_node_test))
                .setValue(testData)
                .addOnSuccessListener(unused -> {
                    isWriteDone = true;
                    checkAndProceed();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Write failed: " + e.getMessage());
                    isWriteDone = true;
                    checkAndProceed();
                });
    }

    private void doFirebaseTestRead() {
        DatabaseReference node = FirebaseDatabase
                .getInstance(getString(R.string.firebase_db_url))
                .getReference()
                .child(getString(R.string.rtdb_node_test));

        node.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    String msg = snap.child(getString(R.string.rtdb_field_message)).getValue(String.class);
                    Long ts    = snap.child(getString(R.string.rtdb_field_timestamp)).getValue(Long.class);
                    Log.d(TAG, getString(R.string.log_message_prefix) + msg);
                    Log.d(TAG, getString(R.string.log_timestamp_prefix) + ts);
                } else {
                    Log.d(TAG, "Test node empty");
                }
                isReadDone = true;
                checkAndProceed();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, getString(R.string.log_error_prefix) + error.getMessage());
                isReadDone = true;
                checkAndProceed();
            }
        });
    }

    private void checkAndProceed() {
        if ((isWriteDone && isReadDone) || isTimeout) {
            goToLogin();
        }
    }

    private void goToLogin() {
        if (isFinishing()) return;
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
