package com.phongkham.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.Specialty;

import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.ViewHolder> {
    private final Context context;
    private final List<Specialty> list;
    private final OnSpecialtyClickListener listener;

    public interface OnSpecialtyClickListener {
        void onSpecialtyClick(Specialty specialty);
    }

    public SpecialtyAdapter(Context context, List<Specialty> list, OnSpecialtyClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_specialty, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Specialty item = list.get(position);
        holder.tvSpecialtyName.setText(item.getName());
        
        // Pick friendly emoji icon based on name
        String icon = "🩺";
        if (item.getName().contains("Tim")) icon = "❤️";
        else if (item.getName().contains("Nhi")) icon = "👶";
        else if (item.getName().contains("Tai Mũi")) icon = "👂";
        else if (item.getName().contains("Da")) icon = "🧴";
        else if (item.getName().contains("Mắt")) icon = "👁️";
        holder.tvSpecialtyIcon.setText(icon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSpecialtyClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpecialtyIcon, tvSpecialtyName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpecialtyIcon = itemView.findViewById(R.id.tvSpecialtyIcon);
            tvSpecialtyName = itemView.findViewById(R.id.tvSpecialtyName);
        }
    }
}
