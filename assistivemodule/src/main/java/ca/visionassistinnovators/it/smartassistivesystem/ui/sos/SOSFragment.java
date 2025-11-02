package ca.visionassistinnovators.it.smartassistivesystem.ui.sos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class SOSFragment extends Fragment {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String AMBULANCE_NUMBER = "911"; // 🚑
    private static final String GUARDIAN_NUMBER = "1234567890"; // 👨‍👩‍👧

    private Button btnAmbulance, btnGuardian;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_sos, container, false);

        btnAmbulance = root.findViewById(R.id.btnCallAmbulance);
        btnGuardian = root.findViewById(R.id.btnCallGuardian);

        // 🚑 Ambulance call button
        btnAmbulance.setOnClickListener(v -> showConfirmDialog("Call Ambulance", AMBULANCE_NUMBER));

        // 👨‍👩‍👧 Guardian call button
        btnGuardian.setOnClickListener(v -> showConfirmDialog("Call Guardian", GUARDIAN_NUMBER));

        return root;
    }

    // ✅ Show confirmation dialog before calling
    private void showConfirmDialog(String title, String phoneNumber) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage("Are you sure you want to call " + title + "?")
                .setPositiveButton("Yes", (dialog, which) -> makePhoneCall(phoneNumber))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ✅ Function to handle calling
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
                Toast.makeText(requireContext(), "Error placing call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ Handle runtime permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted. Tap again to call.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Call permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
