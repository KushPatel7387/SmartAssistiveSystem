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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SosFragment extends Fragment {

    private static final String AMBULANCE_NUMBER = "911"; // Emergency number (Canada)
    private static final String GUARDIAN_NUMBER = "12345678"; // Replace with real number

    private String pendingNumberToCall;

    // ✅ Modern permission launcher
    private final ActivityResultLauncher<String> callPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "✅ Permission granted. Calling...", Toast.LENGTH_SHORT).show();
                    makePhoneCall(pendingNumberToCall);
                } else {
                    Toast.makeText(requireContext(), "❌ Permission denied for phone calls.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sos, container, false);

        Button btnAmbulance = root.findViewById(R.id.btnCallAmbulance);
        Button btnGuardian = root.findViewById(R.id.btnCallGuardian);

        // 🚑 Ambulance button
        btnAmbulance.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "🚑 Calling Ambulance...", Toast.LENGTH_SHORT).show();
            requestCallPermissionAndCall(AMBULANCE_NUMBER);
        });

        // 👨‍👩‍👧‍👦 Guardian button
        btnGuardian.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "📞 Calling Guardian...", Toast.LENGTH_SHORT).show();
            requestCallPermissionAndCall(GUARDIAN_NUMBER);
        });

        return root;
    }

    private void requestCallPermissionAndCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall(phoneNumber);
        } else {
            pendingNumberToCall = phoneNumber;
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "⚠️ Unable to make call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
