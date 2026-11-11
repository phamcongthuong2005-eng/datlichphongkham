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
import com.phongkham.app.models.ClinicRoom;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private Context context;
    private List<ClinicRoom> list;
    private OnRoomDeleteListener listener;

    public interface OnRoomDeleteListener {
        void onDeleteRoom(ClinicRoom room);
    }

    public RoomAdapter(Context context, List<ClinicRoom> list, OnRoomDeleteListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clinic_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClinicRoom room = list.get(position);
        holder.tvRoomCodeName.setText(room.getMaPhong() + " - " + room.getTenPhong());
        holder.tvRoomTypeStatus.setText("Loại: " + room.getLoaiPhong() + " • Trạng thái: " + room.getTrangThai());

        holder.ivDeleteRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteRoom(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomCodeName, tvRoomTypeStatus;
        ImageView ivDeleteRoom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomCodeName = itemView.findViewById(R.id.tvRoomCodeName);
            tvRoomTypeStatus = itemView.findViewById(R.id.tvRoomTypeStatus);
            ivDeleteRoom = itemView.findViewById(R.id.ivDeleteRoom);
        }
    }
}
