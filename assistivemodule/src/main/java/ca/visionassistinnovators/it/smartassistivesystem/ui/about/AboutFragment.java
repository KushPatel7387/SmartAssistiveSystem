/**
 * Course Section: OCA
 * Team Members
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.about;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class AboutFragment extends Fragment {

    private TextView txtVersion;
    private LinearLayout layoutPrivacy, layoutTerms, layoutSupport, layoutLicenses;

    public AboutFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Initialize views
        txtVersion = view.findViewById(R.id.txtVersion);
        layoutPrivacy = view.findViewById(R.id.layoutPrivacy);
        layoutTerms = view.findViewById(R.id.layoutTerms);
        layoutSupport = view.findViewById(R.id.layoutSupport);
        layoutLicenses = view.findViewById(R.id.layoutLicenses);

        // Set version text dynamically
        setAppVersion();

        // Click listeners to open URLs
        layoutPrivacy.setOnClickListener(v ->
                openLink("https://www.privacypolicies.com/live/9cf3e76f-3d24-46e0-af25-6b8f5aaf1f40")
        );

        layoutTerms.setOnClickListener(v ->
                openLink("https://www.termsfeed.com/blog/sample-terms-of-use-template/")
        );

        layoutSupport.setOnClickListener(v ->
                openLink("mailto:support.smartassistive@gmail.com")
        );

        layoutLicenses.setOnClickListener(v ->
                openLink("https://opensource.google/documentation/reference/terms/")
        );

        return view;
    }

    private void setAppVersion() {
        try {
            PackageManager pm = requireActivity().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(requireActivity().getPackageName(), 0);
            txtVersion.setText(pi.versionName);
        } catch (Exception e) {
            txtVersion.setText("1.0.0");
        }
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
