// Dashboard: schedule counts and recently finished appointments.

package com.example.appointmentscheduler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleDashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    RecyclerView finished_recycler;
    FinishedSchedAdapter finishedAdapter;
    ArrayList<String> array_name, array_date, array_time, array_desc, array_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_dashboard);

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(ScheduleDashboardActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(ScheduleDashboardActivity.this, AddAppointmentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(ScheduleDashboardActivity.this, MoreMenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        dbHelper = new DatabaseHelper(ScheduleDashboardActivity.this);

        getCurrentUserName();
        getCurrentScheduleCount();
        getCurrentFinishedScheduleCount();

        finished_recycler = findViewById(R.id.finished_sched_recycler);

        array_name = new ArrayList<>();
        array_date = new ArrayList<>();
        array_time = new ArrayList<>();
        array_desc = new ArrayList<>();
        array_link = new ArrayList<>();

        storeFinishedSched();

        finishedAdapter = new FinishedSchedAdapter(ScheduleDashboardActivity.this, array_name, array_date, array_time, array_desc, array_link);
        finished_recycler.setLayoutManager(new LinearLayoutManager(ScheduleDashboardActivity.this));
        finished_recycler.setAdapter(finishedAdapter);
    }

    void storeFinishedSched() {
        Cursor cursor = dbHelper.readFinishedSchedule();
        try {
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    array_name.add(cursor.getString(1));
                    array_date.add(cursor.getString(2));
                    array_desc.add(cursor.getString(3));
                    array_time.add(cursor.getString(4));
                    array_link.add(cursor.getString(5));
                }
            }
        } finally {
            cursor.close();
        }
    }

    private void getCurrentFinishedScheduleCount() {
        TextView finishedSchedCount = findViewById(R.id.finishedCount);
        int finishedCount = dbHelper.getFinishedScheduleCount();
        finishedSchedCount.setText(String.valueOf(finishedCount));
    }

    private void getCurrentScheduleCount() {
        TextView schedCount = findViewById(R.id.schedCount);
        String totalSchedCount = dbHelper.getTotalAppointmentCount();
        Log.d("ScheduleDashboardActivity", "Retrieved schedule count: " + totalSchedCount);
        schedCount.setText(totalSchedCount);
    }

    private void getCurrentUserName() {
        TextView greetUser = findViewById(R.id.greetUser);
        greetUser.setText("Your schedule");
    }
}
