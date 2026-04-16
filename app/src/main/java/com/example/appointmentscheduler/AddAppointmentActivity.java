// Full-screen form to add an appointment (legacy navigation flow).

package com.example.appointmentscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class AddAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        dbHelper = new DatabaseHelper(this);

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddAppointmentActivity.this, MoreMenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddAppointmentActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddAppointmentActivity.this, ScheduleDashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        Button datePicker = findViewById(R.id.datePicker);
        datePicker.setOnClickListener(view -> dateDialog());

        Button timePicker = findViewById(R.id.timePicker);
        timePicker.setOnClickListener(view -> timeDialog());

        Button submitSchedButton = findViewById(R.id.submitSchedButton);
        submitSchedButton.setOnClickListener(view -> addingSchedule());
    }

    private void dateDialog() {
        TextView dateText = findViewById(R.id.dateText);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String formattedMonth = String.format("%02d", month + 1);
                String formattedDay = String.format("%02d", day);
                dateText.setText(year + "-" + formattedMonth + "-" + formattedDay);
            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();
    }

    private void timeDialog() {
        TextView timeText = findViewById(R.id.timeText);
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                String amPm;
                if (hours >= 12) {
                    amPm = "PM";
                    if (hours > 12) {
                        hours -= 12;
                    }
                } else {
                    amPm = "AM";
                    if (hours == 0) {
                        hours = 12;
                    }
                }
                String formattedMinutes = String.format("%02d", minutes);
                timeText.setText(String.format("%d:%s %s", hours, formattedMinutes, amPm));
            }
        }, 12, 00, false);
        dialog.show();
    }

    private void addingSchedule() {
        EditText inputSchedName = findViewById(R.id.nameSet);
        EditText inputSchedDesc = findViewById(R.id.descriptionSet);
        EditText inputSchedLink = findViewById(R.id.linkSet);
        TextView inputSchedDate = findViewById(R.id.dateText);
        TextView inputSchedTime = findViewById(R.id.timeText);

        String schedName = inputSchedName.getText().toString();
        String schedDesc = inputSchedDesc.getText().toString();
        String schedLink = inputSchedLink.getText().toString();
        String schedDate = inputSchedDate.getText().toString();
        String schedTime = inputSchedTime.getText().toString();

        String errorMessage = "";
        String[] requiredFields = {schedName, schedDate, schedDesc, schedTime};
        String[] requiredFieldNames = {"Name", "Date", "Description", "Time"};

        for (int i = 0; i < requiredFields.length; i++) {
            if (requiredFields[i].isEmpty()) {
                errorMessage += requiredFieldNames[i] + ", ";
            }
        }

        if (!errorMessage.isEmpty()) {
            errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
            Toast.makeText(AddAppointmentActivity.this, "The following fields cannot be empty: " + errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            boolean saved = dbHelper.addAppointment(schedName, schedDate, schedDesc, schedTime, schedLink);
            if (saved) {
                Toast.makeText(AddAppointmentActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                inputSchedName.setText("");
                inputSchedDate.setText("");
                inputSchedTime.setText("");
                inputSchedDesc.setText("");
                inputSchedLink.setText("");
            } else {
                Toast.makeText(AddAppointmentActivity.this, "Error saving schedule", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
