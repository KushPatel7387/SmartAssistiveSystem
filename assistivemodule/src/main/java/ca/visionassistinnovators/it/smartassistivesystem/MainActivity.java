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
import androidx.core.splashscreen.SplashScreenViewProvider;

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
    private static final long MAX_SPLASH_TIME = 4000L;

    private volatile boolean isWriteDone = false;
    private volatile boolean isReadDone = false;
    private volatile boolean isTimeout = false;

    private boolean animationRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        splashScreen.setOnExitAnimationListener(splashScreenView -> {

            View icon = splashScreenView.getIconView();
            if (icon == null) {
                splashScreenView.remove();
                return;
            }

            // 360-degree rotation
            icon.animate()
                    .rotationBy(360f)
                    .setDuration(800)
                    .withEndAction(() -> {
                        icon.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(splashScreenView::remove)
                                .start();
                    })
                    .start();
        });


        // Firebase logic
        doFirebaseTestWrite();
        doFirebaseTestRead();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isTimeout = true;
            checkAndProceed();
        }, MAX_SPLASH_TIME);
    }


    private void startSplashIconAnimationLoop(SplashScreen splash) {

        // START animation only when splash becomes visible
        splash.setOnExitAnimationListener(provider -> {}); // needed hack

        View decor = getWindow().getDecorView();
        decor.post(() -> {
            try {
                View icon = decor.findViewById(android.R.id.icon);

                // If icon can't be found, safely skip animation
                if (icon == null) return;

                animationRunning = true;
                icon.setTranslationX(-300f);

                Runnable loop = new Runnable() {
                    @Override
                    public void run() {
                        if (!animationRunning) return;

                        // Move center → right → left → repeat
                        icon.animate().translationX(0f).setDuration(500).withEndAction(() ->
                                icon.animate().translationX(300f).setDuration(500).withEndAction(() ->
                                        icon.animate().translationX(-300f).setDuration(500).withEndAction(this)
                                ).start()
                        ).start();
                    }
                };

                icon.post(loop);

            } catch (Exception ignored) {}
        });
    }

    private void doFirebaseTestWrite() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Hello from SmartAssistive");
        testData.put("timestamp", System.currentTimeMillis());

        root.child("test_splash").setValue(testData)
                .addOnSuccessListener(unused -> {
                    isWriteDone = true;
                    checkAndProceed();
                })
                .addOnFailureListener(e -> {
                    isWriteDone = true;
                    checkAndProceed();
                });
    }

    private void doFirebaseTestRead() {
        DatabaseReference node = FirebaseDatabase.getInstance()
                .getReference()
                .child("test_splash");

        node.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                isReadDone = true;
                checkAndProceed();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                isReadDone = true;
                checkAndProceed();
            }
        });
    }

    private void checkAndProceed() {
        if ((isWriteDone && isReadDone) || isTimeout) {
            goLogin();
        }
    }

    private void goLogin() {
        if (isFinishing()) return;

        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
