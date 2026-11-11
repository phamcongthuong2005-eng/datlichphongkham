package com.phongkham.app.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.NotificationAdapter;
import com.phongkham.app.database.NotificationDao;
import com.phongkham.app.models.NotificationItem;
import com.phongkham.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvMarkAllRead, tvEmptyNotifications;
    private Button btnFilterAll, btnFilterUnread, btnFilterRead;
    private RecyclerView rvNotifications;

    private NotificationDao notificationDao;
    private SessionManager sessionManager;
    private List<NotificationItem> allList = new ArrayList<>();
    private List<NotificationItem> displayList = new ArrayList<>();
    private NotificationAdapter adapter;
    private String currentFilter = "ALL"; // ALL, UNREAD, READ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationDao = new NotificationDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUnread = findViewById(R.id.btnFilterUnread);
        btnFilterRead = findViewById(R.id.btnFilterRead);
        rvNotifications = findViewById(R.id.rvNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtons();
            applyFilter();
        });

        btnFilterUnread.setOnClickListener(v -> {
            currentFilter = "UNREAD";
            updateFilterButtons();
            applyFilter();
        });

        btnFilterRead.setOnClickListener(v -> {
            currentFilter = "READ";
            updateFilterButtons();
            applyFilter();
        });

        tvMarkAllRead.setOnClickListener(v -> {
            notificationDao.markAllAsRead(sessionManager.getUserId());
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
            loadNotifications();
        });
    }

    private void updateFilterButtons() {
        int activeBg = ContextCompat.getColor(this, R.color.primary);
        int inactiveBg = ContextCompat.getColor(this, R.color.primary_light);
        int activeText = Color.WHITE;
        int inactiveText = ContextCompat.getColor(this, R.color.primary);

        btnFilterAll.setBackgroundTintList(ColorStateList.valueOf("ALL".equals(currentFilter) ? activeBg : inactiveBg));
        btnFilterAll.setTextColor("ALL".equals(currentFilter) ? activeText : inactiveText);

        btnFilterUnread.setBackgroundTintList(ColorStateList.valueOf("UNREAD".equals(currentFilter) ? activeBg : inactiveBg));
        btnFilterUnread.setTextColor("UNREAD".equals(currentFilter) ? activeText : inactiveText);

        btnFilterRead.setBackgroundTintList(ColorStateList.valueOf("READ".equals(currentFilter) ? activeBg : inactiveBg));
        btnFilterRead.setTextColor("READ".equals(currentFilter) ? activeText : inactiveText);
    }

    private void loadNotifications() {
        int userId = sessionManager.getUserId();
        allList = notificationDao.getNotificationsByUserId(userId);
        applyFilter();
    }

    private void applyFilter() {
        displayList.clear();
        for (NotificationItem item : allList) {
            if ("ALL".equals(currentFilter)) {
                displayList.add(item);
            } else if ("UNREAD".equals(currentFilter) && !item.isRead()) {
                displayList.add(item);
            } else if ("READ".equals(currentFilter) && item.isRead()) {
                displayList.add(item);
            }
        }

        if (displayList.isEmpty()) {
            tvEmptyNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmptyNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);

            adapter = new NotificationAdapter(this, displayList, item -> {
                if (!item.isRead()) {
                    notificationDao.markAsRead(item.getId());
                    item.setRead(true);
                    adapter.notifyDataSetChanged();
                }

                new AlertDialog.Builder(this)
                        .setTitle(item.getTitle())
                        .setMessage(item.getContent() + "\n\nThời gian: " + item.getCreatedAt())
                        .setPositiveButton("Đóng", null)
                        .show();
            });
            rvNotifications.setAdapter(adapter);
        }
    }
}
