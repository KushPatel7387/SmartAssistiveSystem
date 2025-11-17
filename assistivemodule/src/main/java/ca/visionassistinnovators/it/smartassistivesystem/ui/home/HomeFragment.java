package ca.visionassistinnovators.it.smartassistivesystem.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class HomeFragment extends Fragment {

    private TextView tvCaption;
    private ImageView imgSlideshow;
    private PieChart pieChartPatients;
    private BarChart barChartSensors;

    private final Handler handler = new Handler();
    private int index = 0;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgSlideshow = view.findViewById(R.id.imgSlideshow);
        tvCaption = view.findViewById(R.id.tv_caption);

        // Initialize charts
        pieChartPatients = view.findViewById(R.id.pieChartPatients);
        barChartSensors = view.findViewById(R.id.barChartSensors);

        startSlideShow();
        setupPieChart();
        setupBarChart();
    }

    // ------------------------------
    // SLIDESHOW
    // ------------------------------
    private void startSlideShow() {
        final int[] images = {
                R.drawable.img_sensor,
                R.drawable.img_alert,
                R.drawable.img_microphone
        };

        final String[] captions = {
                getString(R.string.smart_assistive_system_in_action),
                getString(R.string.sensors_active),
                getString(R.string.voice_assistance)
        };

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getView() == null) return;

                imgSlideshow.setImageResource(images[index % images.length]);
                tvCaption.setText(captions[index % captions.length]);
                index++;

                handler.postDelayed(this, 3500);
            }
        });
    }

    // ------------------------------
    // PIE CHART - PATIENTS
    // ------------------------------
    private void setupPieChart() {

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(60, getString(R.string.active)));
        entries.add(new PieEntry(25, getString(R.string.idle)));
        entries.add(new PieEntry(15, getString(R.string.alerts_)));

        PieDataSet dataSet = new PieDataSet(entries, "Patient Status");
        dataSet.setColors(
                getResources().getColor(R.color.purple_500),
                getResources().getColor(R.color.teal_700),
                getResources().getColor(R.color.red)
        );
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        pieChartPatients.setData(data);
        pieChartPatients.setUsePercentValues(true);
        pieChartPatients.getDescription().setEnabled(false);
        pieChartPatients.invalidate();  // refresh
    }

    // ------------------------------
    // BAR CHART - SENSOR HEALTH
    // ------------------------------
    private void setupBarChart() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 95)); // Sensor 1
        entries.add(new BarEntry(2, 88)); // Sensor 2
        entries.add(new BarEntry(3, 91)); // Sensor 3
        entries.add(new BarEntry(4, 76)); // Sensor 4

        BarDataSet dataSet = new BarDataSet(entries, "Sensor Health (%)");
        dataSet.setColor(getResources().getColor(R.color.purple_500));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartSensors.setData(data);
        barChartSensors.getDescription().setEnabled(false);
        barChartSensors.invalidate(); // refresh
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
