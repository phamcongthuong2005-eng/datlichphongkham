package com.phongkham.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.TimeSlot;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private final Context context;
    private final List<TimeSlot> list;
    private int selectedPosition = -1;
    private final OnSlotSelectedListener listener;

    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlot slot);
    }

    public TimeSlotAdapter(Context context, List<TimeSlot> list, OnSlotSelectedListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeslot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = list.get(position);
        holder.tvSlotTime.setText(slot.getSlotTime());

        boolean isFull = !slot.isAvailable();

        if (isFull) {
            holder.layoutContainer.setBackgroundResource(R.drawable.bg_slot_full);
            holder.tvSlotTime.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvSlotStatus.setText("Đã đầy (" + slot.getBookedPatients() + "/" + slot.getMaxPatients() + ")");
            holder.tvSlotStatus.setTextColor(Color.parseColor("#EF4444"));
        } else if (position == selectedPosition) {
            holder.layoutContainer.setBackgroundResource(R.drawable.bg_slot_selected);
            holder.tvSlotTime.setTextColor(Color.WHITE);
            holder.tvSlotStatus.setText("Đang chọn (" + slot.getBookedPatients() + "/" + slot.getMaxPatients() + ")");
            holder.tvSlotStatus.setTextColor(Color.WHITE);
        } else {
            holder.layoutContainer.setBackgroundResource(R.drawable.bg_slot_available);
            holder.tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.primary));
            holder.tvSlotStatus.setText("Còn trống (" + slot.getBookedPatients() + "/" + slot.getMaxPatients() + ")");
            holder.tvSlotStatus.setTextColor(Color.parseColor("#64748B"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (isFull) {
                Toast.makeText(context, "Khung giờ này đã đầy bệnh nhân. Vui lòng chọn khung giờ khác!", Toast.LENGTH_SHORT).show();
                return;
            }
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSlotSelected(slot);
        });
    }

    public TimeSlot getSelectedSlot() {
        if (selectedPosition >= 0 && selectedPosition < list.size()) {
            return list.get(selectedPosition);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutContainer;
        TextView tvSlotTime, tvSlotStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = itemView.findViewById(R.id.layoutSlotContainer);
            tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
            tvSlotStatus = itemView.findViewById(R.id.tvSlotStatus);
        }
    }
}
