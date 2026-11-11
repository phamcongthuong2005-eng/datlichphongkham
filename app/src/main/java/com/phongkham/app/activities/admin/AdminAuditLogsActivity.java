package com.phongkham.app.activities.admin;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phongkham.app.R;
import com.phongkham.app.adapters.AuditLogAdapter;
import com.phongkham.app.database.AuditLogDao;
import com.phongkham.app.models.AuditLog;

import java.util.List;

public class AdminAuditLogsActivity extends AppCompatActivity {
    private ImageView ivBack;
    private RecyclerView rvAdminAuditLogs;
    private AuditLogDao auditLogDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit_logs);

        auditLogDao = new AuditLogDao(this);

        initViews();
        loadAuditLogs();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvAdminAuditLogs = findViewById(R.id.rvAdminAuditLogs);

        ivBack.setOnClickListener(v -> finish());
        rvAdminAuditLogs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAuditLogs() {
        List<AuditLog> list = auditLogDao.getAllLogs();
        AuditLogAdapter adapter = new AuditLogAdapter(this, list);
        rvAdminAuditLogs.setAdapter(adapter);
    }
}
