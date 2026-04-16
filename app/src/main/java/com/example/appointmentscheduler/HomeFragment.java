package com.example.appointmentscheduler;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private ListView listView;
    private TextView emptyText;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listView = view.findViewById(R.id.list_recent);
        emptyText = view.findViewById(R.id.text_empty);
        fabAdd = view.findViewById(R.id.fab_add);

        fabAdd.setOnClickListener(v -> {
            addAppointmentClicked();
        });

        return view;
    }

    private void addAppointmentClicked() {
    }
}