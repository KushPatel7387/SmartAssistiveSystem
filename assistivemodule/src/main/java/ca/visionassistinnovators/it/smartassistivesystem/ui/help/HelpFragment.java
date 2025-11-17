package ca.visionassistinnovators.it.smartassistivesystem.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HelpFragment extends Fragment {

    private static final String HELP_URL = "https://support.google.com/accessibility/android";

    public HelpFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);

        WebView webView = root.findViewById(R.id.helpWebView);
        webView.setWebViewClient(new WebViewClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.loadUrl(HELP_URL);

        return root;
    }
}
