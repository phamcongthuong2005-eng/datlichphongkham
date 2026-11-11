package com.phongkham.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.Appointment;
import com.phongkham.app.models.User;
import com.phongkham.app.utils.DateTimeUtils;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final Context context;
    private final List<Appointment> list;
    private final String currentRole;
    private final OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onPrimaryAction(Appointment appointment);
        void onSecondaryAction(Appointment appointment);
        void onItemClick(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> list, String currentRole, OnAppointmentActionListener listener) {
        this.context = context;
        this.list = list;
        this.currentRole = currentRole;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = list.get(position);
        holder.tvAppointmentCode.setText("Mã: " + a.getAppointmentCode());
        holder.tvDateTime.setText("📅 " + a.getAppointmentDate() + " • ⏰ " + a.getTimeSlot());
        holder.tvSymptoms.setText("Lý do khám: " + (a.getSymptoms() != null ? a.getSymptoms() : "Không có"));

        if (a.getQueueNumber() > 0) {
            holder.tvQueueNumber.setVisibility(View.VISIBLE);
            holder.tvQueueNumber.setText("Số khám: #" + a.getQueueNumber());
        } else {
            holder.tvQueueNumber.setVisibility(View.GONE);
        }

        // Display doctor or patient name depending on role
        if (User.ROLE_DOCTOR.equals(currentRole)) {
            holder.tvDoctorOrPatientName.setText("Bệnh nhân: " + a.getPatientName() + " (SĐT: " + a.getPatientPhone() + ")");
        } else {
            holder.tvDoctorOrPatientName.setText("Bác sĩ: " + a.getDoctorName() + " (" + a.getSpecialtyName() + ")");
        }

        // Status badge color & text
        holder.tvStatusBadge.setText(DateTimeUtils.getStatusVietnamese(a.getStatus()));
        switch (a.getStatus()) {
            case Appointment.STATUS_CONFIRMED:
                holder.tvStatusBadge.setTextColor(Color.parseColor("#0284C7"));
                break;
            case Appointment.STATUS_EXAMINING:
                holder.tvStatusBadge.setTextColor(Color.parseColor("#8B5CF6"));
                break;
            case Appointment.STATUS_COMPLETED:
                holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                break;
            case Appointment.STATUS_CANCELLED:
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                break;
            default:
                holder.tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                break;
        }

        // Dynamic buttons per role & status
        setupActionButtons(holder, a);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(a);
        });
    }

    private void setupActionButtons(ViewHolder holder, Appointment a) {
        holder.layoutActionButtons.setVisibility(View.VISIBLE);
        holder.btnActionPrimary.setVisibility(View.VISIBLE);
        holder.btnActionSecondary.setVisibility(View.VISIBLE);

        if (User.ROLE_PATIENT.equals(currentRole)) {
            if (Appointment.STATUS_PENDING.equals(a.getStatus()) || Appointment.STATUS_CONFIRMED.equals(a.getStatus())) {
                holder.btnActionSecondary.setText("Hủy lịch");
                holder.btnActionPrimary.setText("Xem chi tiết");
            } else if (Appointment.STATUS_COMPLETED.equals(a.getStatus())) {
                holder.btnActionSecondary.setText("Đánh giá");
                holder.btnActionPrimary.setText("Xem đơn thuốc");
            } else {
                holder.btnActionSecondary.setVisibility(View.GONE);
                holder.btnActionPrimary.setText("Xem chi tiết");
            }
        } else if (User.ROLE_DOCTOR.equals(currentRole)) {
            if (Appointment.STATUS_CONFIRMED.equals(a.getStatus()) || Appointment.STATUS_PENDING.equals(a.getStatus())) {
                holder.btnActionSecondary.setText("Báo vắng");
                holder.btnActionPrimary.setText("Bắt đầu khám");
            } else if (Appointment.STATUS_EXAMINING.equals(a.getStatus())) {
                holder.btnActionSecondary.setVisibility(View.GONE);
                holder.btnActionPrimary.setText("Tiếp tục khám");
            } else if (Appointment.STATUS_COMPLETED.equals(a.getStatus())) {
                holder.btnActionSecondary.setVisibility(View.GONE);
                holder.btnActionPrimary.setText("Xem hồ sơ");
            } else {
                holder.layoutActionButtons.setVisibility(View.GONE);
            }
        } else if (User.ROLE_STAFF.equals(currentRole)) {
            if (Appointment.STATUS_PENDING.equals(a.getStatus())) {
                holder.btnActionSecondary.setText("Hủy lịch");
                holder.btnActionPrimary.setText("Tiếp nhận & Cấp số");
            } else if (Appointment.STATUS_COMPLETED.equals(a.getStatus())) {
                holder.btnActionSecondary.setVisibility(View.GONE);
                holder.btnActionPrimary.setText("Lập hóa đơn");
            } else {
                holder.btnActionSecondary.setVisibility(View.GONE);
                holder.btnActionPrimary.setText("Chi tiết");
            }
        } else {
            // ADMIN
            holder.btnActionSecondary.setText("Hủy lịch");
            holder.btnActionPrimary.setText("Chi tiết");
        }

        holder.btnActionPrimary.setOnClickListener(v -> {
            if (listener != null) listener.onPrimaryAction(a);
        });

        holder.btnActionSecondary.setOnClickListener(v -> {
            if (listener != null) listener.onSecondaryAction(a);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentCode, tvStatusBadge, tvDoctorOrPatientName, tvDateTime, tvQueueNumber, tvSymptoms;
        LinearLayout layoutActionButtons;
        Button btnActionSecondary, btnActionPrimary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppointmentCode = itemView.findViewById(R.id.tvAppointmentCode);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDoctorOrPatientName = itemView.findViewById(R.id.tvDoctorOrPatientName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvQueueNumber = itemView.findViewById(R.id.tvQueueNumber);
            tvSymptoms = itemView.findViewById(R.id.tvSymptoms);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnActionSecondary = itemView.findViewById(R.id.btnActionSecondary);
            btnActionPrimary = itemView.findViewById(R.id.btnActionPrimary);
        }
    }
}
