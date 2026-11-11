package com.phongkham.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context context;
    private final List<User> list;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onToggleLock(User user);
    }

    public UserAdapter(Context context, List<User> list, OnUserActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = list.get(position);
        holder.tvUserFullName.setText(u.getFullName());
        holder.tvUserEmailPhone.setText((u.getPhone() != null ? u.getPhone() : "") + " • " + (u.getEmail() != null ? u.getEmail() : ""));
        holder.tvUserRoleBadge.setText("Vai trò: " + u.getRole() + (u.isLocked() ? " (ĐÃ KHÓA)" : ""));

        if (u.isLocked()) {
            holder.btnToggleLock.setText("Mở khóa");
            holder.btnToggleLock.setBackgroundColor(Color.parseColor("#D1FAE5"));
            holder.btnToggleLock.setTextColor(Color.parseColor("#059669"));
        } else {
            holder.btnToggleLock.setText("Khóa");
            holder.btnToggleLock.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.btnToggleLock.setTextColor(Color.parseColor("#EF4444"));
        }

        holder.btnToggleLock.setOnClickListener(v -> {
            if (listener != null) listener.onToggleLock(u);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserFullName, tvUserEmailPhone, tvUserRoleBadge;
        Button btnToggleLock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvUserEmailPhone = itemView.findViewById(R.id.tvUserEmailPhone);
            tvUserRoleBadge = itemView.findViewById(R.id.tvUserRoleBadge);
            btnToggleLock = itemView.findViewById(R.id.btnToggleLock);
        }
    }
}
