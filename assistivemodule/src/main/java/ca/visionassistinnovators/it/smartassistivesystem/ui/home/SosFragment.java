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
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class SosFragment extends Fragment {

    private String pendingNumberToCall;
    private ActivityResultLauncher<String> callPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sos, container, false);

        Button btnAmbulance = root.findViewById(R.id.btnCallAmbulance);
        Button btnGuardian  = root.findViewById(R.id.btnCallGuardian);

        // Register permission callback
        callPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), R.string.call_perm_granted, Toast.LENGTH_SHORT).show();
                        makePhoneCall(pendingNumberToCall);
                    } else {
                        Toast.makeText(requireContext(), R.string.call_perm_denied, Toast.LENGTH_SHORT).show();
                        // Optional fallback: open dialer so user can still place the call manually
                        if (pendingNumberToCall != null) openDialer(pendingNumberToCall);
                    }
                });

        btnAmbulance.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.calling_ambulance, Toast.LENGTH_SHORT).show();
            String ambulance = getString(R.string.ambulance_number);
            requestCallPermissionAndCall(ambulance);
        });

        btnGuardian.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.calling_guardian, Toast.LENGTH_SHORT).show();
            String guardian = getGuardianNumber();
            requestCallPermissionAndCall(guardian);
        });

        return root;
    }

    private String getGuardianNumber() {
        // Read from SharedPreferences; if absent, use default from strings.xml
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String def = getString(R.string.guardian_number_default);
        return prefs.getString(getString(R.string.pref_key_guardian_number), def);
    }

    private void requestCallPermissionAndCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall(phoneNumber);
        } else {
            pendingNumberToCall = phoneNumber;
            // Optional: show your own UI rationale before launching permission
            Toast.makeText(requireContext(), R.string.call_perm_rationale, Toast.LENGTH_SHORT).show();
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (SecurityException se) {
            // If permission missing for some reason, fall back to dialer
            openDialer(phoneNumber);
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    getString(R.string.unable_to_call, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialer(String phoneNumber) {
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(dial);
    }
}
