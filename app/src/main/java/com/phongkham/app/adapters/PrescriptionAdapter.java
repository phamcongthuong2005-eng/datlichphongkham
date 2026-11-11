package com.phongkham.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.PrescriptionItem;

import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {
    private final Context context;
    private final List<PrescriptionItem> list;

    public PrescriptionAdapter(Context context, List<PrescriptionItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionItem item = list.get(position);
        holder.tvPrescriptionMedName.setText((position + 1) + ". " + item.getMedicineName());
        holder.tvPrescriptionQuantity.setText(item.getQuantity() + " " + item.getUnit());
        holder.tvPrescriptionDosage.setText("Liều dùng: " + item.getDosage() + " • " + item.getInstructions());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrescriptionMedName, tvPrescriptionQuantity, tvPrescriptionDosage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrescriptionMedName = itemView.findViewById(R.id.tvPrescriptionMedName);
            tvPrescriptionQuantity = itemView.findViewById(R.id.tvPrescriptionQuantity);
            tvPrescriptionDosage = itemView.findViewById(R.id.tvPrescriptionDosage);
        }
    }
}
