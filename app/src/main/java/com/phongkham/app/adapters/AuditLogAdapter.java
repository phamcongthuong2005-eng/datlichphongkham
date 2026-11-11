package com.phongkham.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.AuditLog;

import java.util.List;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.ViewHolder> {
    private final Context context;
    private final List<AuditLog> list;

    public AuditLogAdapter(Context context, List<AuditLog> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audit_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLog log = list.get(position);
        holder.tvAuditAction.setText("[" + log.getAction() + "]");
        holder.tvAuditTimestamp.setText(log.getTimestamp());
        holder.tvAuditUser.setText("Thực hiện: " + log.getUserName() + " (" + log.getUserRole() + ")");
        holder.tvAuditDetails.setText(log.getDetails());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuditAction, tvAuditTimestamp, tvAuditUser, tvAuditDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuditAction = itemView.findViewById(R.id.tvAuditAction);
            tvAuditTimestamp = itemView.findViewById(R.id.tvAuditTimestamp);
            tvAuditUser = itemView.findViewById(R.id.tvAuditUser);
            tvAuditDetails = itemView.findViewById(R.id.tvAuditDetails);
        }
    }
}
