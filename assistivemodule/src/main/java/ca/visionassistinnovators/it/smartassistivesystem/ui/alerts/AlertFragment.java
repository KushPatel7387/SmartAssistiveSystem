/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util.EventLogger;

public class AlertFragment extends Fragment implements AlertsManager.AlertsListener {

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

        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        alertsManager = new AlertsManager();

        // 🔐 Get current signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    R.string.no_signed_in_user_for_alerts,
                    Toast.LENGTH_SHORT).show();
            tvEmpty.setText(R.string.sign_in_to_see_alerts);
            tvEmpty.setVisibility(View.VISIBLE);

            // Analytics: alert screen opened without sign-in
            EventLogger.logScreenView(requireContext(), getString(R.string.alertfragment_no_user));
            return;
        }

        currentUid = user.getUid();

        // ✅ Adapter create karo have, UID mali gayu
        adapter = new AlertsAdapter(requireContext(), currentUid);
        rvAlerts.setAdapter(adapter);

        // Analytics: screen view
        EventLogger.logScreenView(requireContext(), getString(R.string.alertfragment));

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
    // AlertsManager.AlertsListener
    // -----------------------------------
    @Override
    public void onAlertsChanged(List<AlertModel> alerts) {
        if (!isAdded()) return;

        adapter.submitList(alerts);

        if (alerts == null || alerts.isEmpty()) {
            tvEmpty.setText(R.string.no_alerts_yet);
            tvEmpty.setVisibility(View.VISIBLE);

            EventLogger.logEvent(
                    requireContext(),
                    getString(R.string.alerts_empty),
                    getString(R.string.no_alerts_found_for_this_guardian)
            );
        } else {
            tvEmpty.setVisibility(View.GONE);

            EventLogger.logEvent(
                    requireContext(),
                    getString(R.string.alerts_loaded),
                    getString(R.string.loaded) + alerts.size() + getString(R.string.alert)
            );
        }
    }

    @Override
    public void onError(String error) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(),
                getString(R.string.failed_to_load_alerts) + error,
                Toast.LENGTH_SHORT).show();

        EventLogger.logEvent(
                requireContext(),
                getString(R.string.alerts_load_error),
                error
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        EventLogger.logScreenView(requireContext(), getString(R.string.alert__));
    }
}
