/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.alerts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.AlertModel;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.AlertsAdapter;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.AlertsManager;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.NotificationHelper;

public class AlertFragment extends Fragment implements AlertsManager.AlertsListener {

    private static final int REQ_POST_NOTIFICATIONS = 2001;

    private TextView tvEmpty;
    private AlertsAdapter adapter;
    private AlertsManager alertsManager;
    private String currentUid;

    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        RecyclerView rvAlerts = root.findViewById(R.id.rv_alerts);
        tvEmpty  = root.findViewById(R.id.tv_alerts_empty);

        // 🔔 Test Notification button (for API 33 permission demo)
        Button btnTestNotification = root.findViewById(R.id.btn_test_notification);
        btnTestNotification.setOnClickListener(v -> handleTestNotificationClick());

        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AlertsAdapter();
        rvAlerts.setAdapter(adapter);

        alertsManager = new AlertsManager();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    R.string.no_signed_in_user_for_alerts,
                    Toast.LENGTH_SHORT).show();
            tvEmpty.setText(R.string.sign_in_to_see_alerts);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        currentUid = user.getUid();

        // This will also auto-seed sample alerts if none exist
        alertsManager.listenForAlerts(requireContext(), currentUid, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertsManager != null && currentUid != null) {
            alertsManager.stopListening(requireContext(), currentUid);
        }
    }

    // -----------------------------------
    // 🔔 Test Notification (API 33 logic)
    // -----------------------------------
    private void handleTestNotificationClick() {
        if (!isAdded()) return;

        // API 33+ → must have POST_NOTIFICATIONS runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int granted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
            );

            if (granted != PackageManager.PERMISSION_GRANTED) {
                // Ask user for permission
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS
                );
                return;
            }
        }

        // Permission already granted OR not required (<33)
        NotificationHelper.showTestAlertNotification(requireContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // User accepted → fire test notification now
                if (isAdded()) {
                    NotificationHelper.showTestAlertNotification(requireContext());
                }

            } else {
                // User denied → show short message
                if (isAdded()) {
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.notifications_permission_denied),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    // -----------------------------------
    // AlertsManager.AlertsListener
    // -----------------------------------
    @Override
    public void onAlertsChanged(List<AlertModel> alerts) {
        if (!isAdded()) return;

        adapter.submitList(alerts);

        if (alerts == null || alerts.isEmpty()) {
            tvEmpty.setText(R.string.no_alerts_yet);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(String error) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(),
                getString(R.string.failed_to_load_alerts) + error,
                Toast.LENGTH_SHORT).show();
    }
}
