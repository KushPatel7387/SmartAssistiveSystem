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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Install Splash Screen API
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // ✅ Delay 3 seconds → go to LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);


        // ✅ Connect directly to your test DB
        DatabaseReference dbRef = FirebaseDatabase
                .getInstance("https://smartassistivesystem-39072-default-rtdb.firebaseio.com")
                .getReference();

        // ✅ Write a test value
        Map<String, Object> testData = new HashMap<>();
        testData.put(getString(R.string.message), getString(R.string.hello_from_android));

        testData.put(getString(R.string.timestamp), System.currentTimeMillis());

        dbRef.child(getString(R.string.test)).setValue(testData);

        // ✅ Read back the data
        dbRef.child(getString(R.string.test1)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.child(getString(R.string.message2)).getValue(String.class);
                    Long time = snapshot.child(getString(R.string.timestamp2)).getValue(Long.class);
                    System.out.println(getString(R.string.message3) + message);
                    System.out.println(getString(R.string.timestamp3) + time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println(getString(R.string.error) + error.getMessage());
            }
        });
    }
}
