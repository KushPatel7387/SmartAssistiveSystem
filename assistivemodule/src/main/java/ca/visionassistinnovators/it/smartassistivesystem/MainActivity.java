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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

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

    private static final long MAX_SPLASH_TIME = 1000; // 2 sec max
    private boolean isWriteDone = false;
    private boolean isReadDone = false;
    private boolean isTimeout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Keep splash until BOTH write & read done OR timeout
        splashScreen.setKeepOnScreenCondition(() ->
                !(isWriteDone && isReadDone) && !isTimeout
        );

        // Start both operations
        doFirebaseTestWrite();
        doFirebaseTestRead();

        // Safety timeout
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isTimeout = true;
            goToLogin();
        }, MAX_SPLASH_TIME);
    }

    private void doFirebaseTestWrite() {
        DatabaseReference dbRef = FirebaseDatabase
                .getInstance(getString(R.string.https_smartassistivesystem_39072_default_rtdb_firebaseio_com))
                .getReference();

        Map<String, Object> testData = new HashMap<>();
        testData.put(getString(R.string.message), getString(R.string.hello_from_android));
        testData.put(getString(R.string.timestamp), System.currentTimeMillis());

        dbRef.child(getString(R.string.test)).setValue(testData)
                .addOnSuccessListener(aVoid -> {
                    isWriteDone = true;
                    checkAndProceed();
                })
                .addOnFailureListener(e -> {
                    isWriteDone = true; // Don't block user
                    checkAndProceed();
                });
    }

    private void doFirebaseTestRead() {
        DatabaseReference dbRef = FirebaseDatabase
                .getInstance(getString(R.string.https_smartassistivesystem_39072_default_rtdb_firebaseio_com))
                .getReference();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.child(getString(R.string.message2)).getValue(String.class);
                    Long time = snapshot.child(getString(R.string.timestamp2)).getValue(Long.class);
                    System.out.println(getString(R.string.message3) + message);
                    System.out.println(getString(R.string.timestamp3) + time);
                }
                isReadDone = true;
                checkAndProceed();

                // Remove listener after first read
                dbRef.child(getString(R.string.test1)).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println(getString(R.string.error) + error.getMessage());
                isReadDone = true;
                checkAndProceed();
                dbRef.child(getString(R.string.test1)).removeEventListener(this);
            }
        };

        dbRef.child(getString(R.string.test1)).addValueEventListener(listener);
    }

    private void checkAndProceed() {
        if ((isWriteDone && isReadDone) || isTimeout) {
            goToLogin();
        }
    }

    private void goToLogin() {
        if (isFinishing()) return;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}