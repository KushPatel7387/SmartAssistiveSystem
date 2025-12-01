/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 *
 * RecyclerView adapter for Alerts list (with delete button).
 */
package ca.visionassistinnovators.it.smartassistivesystem.businesslogic;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private final List<AlertModel> items = new ArrayList<>();
    private final DateFormat dateFormat =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    private final Context context;
    private final String userId;   // current guardian UID

    // Adapter needs context + userId (for Firebase path)
    public AlertsAdapter(@NonNull Context context, @NonNull String userId) {
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertModel item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);

        String formattedTime = dateFormat.format(new Date(item.timestamp));
        holder.tvTime.setText(formattedTime);

        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(context)
                    .setTitle("Delete alert?")
                    .setMessage("This alert will be removed permanently.")
                    .setPositiveButton("Delete", (dialog, which) ->
                            deleteAlertFromFirebase(item))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Replace full list from AlertsManager
    public void submitList(List<AlertModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvMessage;
        final TextView tvTime;
        final ImageButton btnDelete;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_alert_title);
            tvMessage = itemView.findViewById(R.id.tv_alert_message);
            tvTime = itemView.findViewById(R.id.tv_alert_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_alert);
        }
    }

    // 🔥 Firebase delete — list will be refreshed from AlertsManager listener
    private void deleteAlertFromFirebase(AlertModel alert) {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                context.getString(R.string.firebase_db_url)
        );

        // Path: /users/{userId}/alerts/{alertId}
        DatabaseReference ref = db.getReference("users")
                .child(userId)
                .child("alerts")
                .child(alert.id);   // id = Firebase key

        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Alert deleted", Toast.LENGTH_SHORT).show();
                // Do NOT manually remove from items here – AlertsManager will push updated list
            } else {
                String msg = "Delete failed";
                if (task.getException() != null) {
                    msg += ": " + task.getException().getMessage();
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
