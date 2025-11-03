/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SosFragment extends Fragment {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String AMBULANCE_NUMBER = "911"; // Emergency number (Canada)
    private static final String GUARDIAN_NUMBER = "1234567890"; // Replace with real number

    private Button btnAmbulance, btnGuardian;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sos, container, false);

        btnAmbulance = root.findViewById(R.id.btnCallAmbulance);
        btnGuardian = root.findViewById(R.id.btnCallGuardian);

        // 🚑 Ambulance button
        btnAmbulance.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "🚑 Calling Ambulance...", Toast.LENGTH_SHORT).show();
            makePhoneCall(AMBULANCE_NUMBER);
        });

        // 👨‍👩‍👧‍👦 Guardians button
        btnGuardian.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "📞 Calling Guardian...", Toast.LENGTH_SHORT).show();
            makePhoneCall(GUARDIAN_NUMBER);
        });

        return root;
    }

    private void makePhoneCall(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "⚠️ Unable to make call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "✅ Permission granted. Tap again to call.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "❌ Permission denied for phone calls.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
