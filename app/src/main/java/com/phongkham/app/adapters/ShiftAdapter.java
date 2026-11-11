package com.phongkham.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.ShiftAssignment;

import java.util.List;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ViewHolder> {
    private Context context;
    private List<ShiftAssignment> list;
    private OnShiftDeleteListener listener;

    public interface OnShiftDeleteListener {
        void onDeleteShift(ShiftAssignment shift);
    }

    public ShiftAdapter(Context context, List<ShiftAssignment> list, OnShiftDeleteListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShiftAssignment shift = list.get(position);
        holder.tvShiftCode.setText(shift.getMaCa() != null ? shift.getMaCa() : "CA LÀM");
        holder.tvShiftTime.setText(shift.getGioBatDau() + " - " + shift.getGioKetThuc() + " (" + shift.getNgayLam() + ")");
        holder.tvShiftRoom.setText("🚪 Phòng trực: " + shift.getMaPhong() + " - " + shift.getTenPhong());
        holder.tvShiftDoctor.setText("👨‍⚕️ Bác sĩ: " + shift.getTenBacSi());
        holder.tvShiftNurse.setText("👩‍⚕️ Y tá phụ trách: " + (shift.getTenYTa() != null ? shift.getTenYTa() : "Chưa phân công"));

        holder.ivDeleteShift.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteShift(shift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvShiftCode, tvShiftTime, tvShiftRoom, tvShiftDoctor, tvShiftNurse;
        ImageView ivDeleteShift;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShiftCode = itemView.findViewById(R.id.tvShiftCode);
            tvShiftTime = itemView.findViewById(R.id.tvShiftTime);
            tvShiftRoom = itemView.findViewById(R.id.tvShiftRoom);
            tvShiftDoctor = itemView.findViewById(R.id.tvShiftDoctor);
            tvShiftNurse = itemView.findViewById(R.id.tvShiftNurse);
            ivDeleteShift = itemView.findViewById(R.id.ivDeleteShift);
        }
    }
}
