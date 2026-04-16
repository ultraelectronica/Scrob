package com.example.appointmentscheduler;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ListView listView;
    private TextView emptyText;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> appointmentItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listView = view.findViewById(R.id.list_recent);
        emptyText = view.findViewById(R.id.text_empty);
        fabAdd = view.findViewById(R.id.fab_add);
        dbHelper = new DatabaseHelper(requireContext());
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, appointmentItems);
        listView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> addAppointmentClicked());
        loadAppointments();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppointments();
    }

    private void addAppointmentClicked() {
        AddAppointmentBottomSheet bottomSheet = new AddAppointmentBottomSheet();
        bottomSheet.setOnAppointmentAddedListener(this::loadAppointments);
        bottomSheet.show(getParentFragmentManager(), "add_appointment_bottom_sheet");
    }

    @SuppressLint("Range")
    private void loadAppointments() {
        if (dbHelper == null) {
            return;
        }

        String currentUsername = dbHelper.getCurrentUsername();
        appointmentItems.clear();

        if (currentUsername != null) {
            Cursor cursor = dbHelper.readUpcomingScheduleByUser(currentUsername);
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                    String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                    String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME));
                    appointmentItems.add(time + "  " + date + "  -  " + name);
                }
            } finally {
                cursor.close();
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        emptyText.setVisibility(appointmentItems.isEmpty() ? View.VISIBLE : View.GONE);
        listView.setVisibility(appointmentItems.isEmpty() ? View.GONE : View.VISIBLE);
    }
}