/**
 * Course Section: OCA
 * Team Members:
 * Sarang Prajapati – N01662036
 * Krish Patel – N01666556
 * Kush Patel – N01657387
 * Daksh Rana – N01664095
 */
package ca.visionassistinnovators.it.smartassistivesystem.ui.alerts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.AlertModel;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private final List<AlertModel> items = new ArrayList<>();
    private final DateFormat dateFormat =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

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

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_alert_title);
            tvMessage = itemView.findViewById(R.id.tv_alert_message);
            tvTime = itemView.findViewById(R.id.tv_alert_time);
        }
    }
}
