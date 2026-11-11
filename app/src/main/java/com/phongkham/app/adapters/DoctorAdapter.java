package com.phongkham.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.Doctor;
import com.phongkham.app.utils.DateTimeUtils;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
    private final Context context;
    private final List<Doctor> list;
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
        void onBookClick(Doctor doctor);
    }

    public DoctorAdapter(Context context, List<Doctor> list, OnDoctorClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = list.get(position);
        holder.tvDoctorName.setText(doc.getFullName());
        holder.tvDoctorSpecialty.setText(doc.getSpecialtyName() + " • " + doc.getExperienceYears() + " năm kinh nghiệm");
        holder.tvDoctorRating.setText("⭐ " + String.format("%.1f", doc.getRating()));
        holder.tvDoctorFee.setText(DateTimeUtils.formatCurrency(doc.getConsultationFee()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoctorClick(doc);
        });

        holder.btnBookDoctor.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(doc);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDoctorSpecialty, tvDoctorRating, tvDoctorFee;
        Button btnBookDoctor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvDoctorRating = itemView.findViewById(R.id.tvDoctorRating);
            tvDoctorFee = itemView.findViewById(R.id.tvDoctorFee);
            btnBookDoctor = itemView.findViewById(R.id.btnBookDoctor);
        }
    }
}
