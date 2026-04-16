package com.example.appointmentscheduler;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewFragment extends Fragment {

    private static final int PAGE_SIZE = 10;

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private TextView subtitleText;
    private TextView pageIndicator;
    private View paginationBar;
    private ImageButton btnPagePrev;
    private ImageButton btnPageNext;

    private DatabaseHelper db;
    private ScheduleAdapter adapter;
    private final List<Appointment> appointments = new ArrayList<>();
    private int currentPage = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.list_appointments);
        emptyState = view.findViewById(R.id.empty_state);
        subtitleText = view.findViewById(R.id.text_view_subtitle);
        paginationBar = view.findViewById(R.id.pagination_bar);
        pageIndicator = view.findViewById(R.id.text_page_indicator);
        btnPagePrev = view.findViewById(R.id.btn_page_prev);
        btnPageNext = view.findViewById(R.id.btn_page_next);

        adapter = new ScheduleAdapter(requireContext(), appointments);
        adapter.setOnItemClickListener(this::openAppointmentDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View child,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = (int) (8 * getResources().getDisplayMetrics().density);
            }
        });

        btnPagePrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadAppointments();
                recyclerView.smoothScrollToPosition(0);
            }
        });
        btnPageNext.setOnClickListener(v -> {
            int total = db.getAppointmentTableCount();
            int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadAppointments();
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void openAppointmentDetail(Appointment appointment) {
        Intent intent = new Intent(requireContext(), UpdateActivity.class);
        intent.putExtra("id", String.valueOf(appointment.getId()));
        startActivity(intent);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private void loadAppointments() {
        int total = db.getAppointmentTableCount();
        updateSubtitle(total);

        int totalPages = total == 0 ? 0 : (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages > 0 && currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        appointments.clear();
        Cursor cursor = null;
        try {
            if (total > 0) {
                cursor = db.getAppointmentsPage(PAGE_SIZE, currentPage * PAGE_SIZE);
                if (cursor != null) {
                    appendRows(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        boolean hasRows = !appointments.isEmpty();
        if (hasRows) {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();

        if (totalPages <= 1) {
            paginationBar.setVisibility(View.GONE);
        } else {
            paginationBar.setVisibility(View.VISIBLE);
            pageIndicator.setText(getString(R.string.view_page_indicator, currentPage + 1, totalPages));
            btnPagePrev.setEnabled(currentPage > 0);
            btnPagePrev.setAlpha(currentPage > 0 ? 1f : 0.35f);
            btnPageNext.setEnabled(currentPage < totalPages - 1);
            btnPageNext.setAlpha(currentPage < totalPages - 1 ? 1f : 0.35f);
        }
    }

    private void updateSubtitle(int total) {
        if (total == 0) {
            subtitleText.setText(R.string.view_appointments_subtitle_hint);
        } else {
            subtitleText.setText(getString(R.string.view_appointments_subtitle_count, total));
        }
    }

    private void appendRows(@NonNull Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDID);
        int idxName = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME);
        int idxDate = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE);
        int idxTime = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME);
        int idxLink = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINK);
        int idxDesc = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idxId);
            String title = nullToEmpty(cursor.getString(idxName));
            String date = nullToEmpty(cursor.getString(idxDate));
            String time = nullToEmpty(cursor.getString(idxTime));
            String link = nullToEmpty(cursor.getString(idxLink));
            String description = nullToEmpty(cursor.getString(idxDesc));
            appointments.add(new Appointment(id, title, date, time, link, description));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppointments();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
