package ca.visionassistinnovators.it.smartassistivesystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private static final long MAX_SPLASH_TIME = 4000L;

    private volatile boolean isWriteDone = false;
    private volatile boolean isReadDone  = false;
    private volatile boolean isTimeout   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ INSTALL SPLASH SAFELY
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        splashScreen.setOnExitAnimationListener(splashScreenView -> {

            // ✅ FULLY TEST-SAFE + EMULATOR-SAFE NULL PROTECTION
            if (splashScreenView == null) {
                return;
            }

            View icon = splashScreenView.getIconView();

            if (icon == null || icon.getWindowToken() == null) {
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

        // ✅ FIREBASE TEST LOGIC
        doFirebaseTestWrite();
        doFirebaseTestRead();

        // ✅ SPLASH TIMEOUT SAFETY (4s MAX)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isTimeout = true;
            checkAndProceed();
        }, MAX_SPLASH_TIME);
    }

    // ---------------------------------------------------------
    // ✅ FIREBASE WRITE TEST
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // ✅ FIREBASE READ TEST
    // ---------------------------------------------------------
    private void doFirebaseTestRead() {

        DatabaseReference node = FirebaseDatabase.getInstance()
                .getReference()
                .child("test_splash");

        node.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isReadDone = true;
                checkAndProceed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isReadDone = true;
                checkAndProceed();
            }
        });
    }

    // ---------------------------------------------------------
    // ✅ PROCEED WHEN BOTH DONE OR TIMEOUT
    // ---------------------------------------------------------
    private void checkAndProceed() {

        if ((isWriteDone && isReadDone) || isTimeout) {
            goLogin();
        }
    }

    // ---------------------------------------------------------
    // ✅ MOVE TO LOGIN
    // ---------------------------------------------------------
    private void goLogin() {

        if (isFinishing()) return;

        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        finish();
    }
}
