package ca.visionassistinnovators.it.smartassistivesystem.ui.patients;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.PatientModel;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    public interface OnPatientActionListener {
        void onCallClicked(PatientModel patient);
        void onDeleteClicked(PatientModel patient);
    }

    private final List<PatientModel> patients = new ArrayList<>();
    private final OnPatientActionListener listener;

    public PatientAdapter(OnPatientActionListener listener) {
        this.listener = listener;
    }

    public void setPatients(List<PatientModel> newPatients) {
        patients.clear();
        if (newPatients != null) {
            patients.addAll(newPatients);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientModel model = patients.get(position);

        holder.tvName.setText(model.fullName != null ? model.fullName : "Unknown");
        holder.tvPhone.setText(model.phone != null ? model.phone : "");

        // Sensor placeholders
        holder.tvSensor1.setText("S1: " + (model.sensor1 != null ? model.sensor1 : "--"));
        holder.tvSensor2.setText("S2: " + (model.sensor2 != null ? model.sensor2 : "--"));
        holder.tvSensor3.setText("S3: " + (model.sensor3 != null ? model.sensor3 : "--"));
        holder.tvSensor4.setText("S4: " + (model.sensor4 != null ? model.sensor4 : "--"));

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClicked(model);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvSensor1, tvSensor2, tvSensor3, tvSensor4;
        ImageButton btnCall, btnDelete;

        PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tv_patient_name);
            tvPhone   = itemView.findViewById(R.id.tv_patient_phone);
            tvSensor1 = itemView.findViewById(R.id.tv_sensor1);
            tvSensor2 = itemView.findViewById(R.id.tv_sensor2);
            tvSensor3 = itemView.findViewById(R.id.tv_sensor3);
            tvSensor4 = itemView.findViewById(R.id.tv_sensor4);
            btnCall   = itemView.findViewById(R.id.btn_call_patient);
            btnDelete = itemView.findViewById(R.id.btn_delete_patient);
        }
    }
}
