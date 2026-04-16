package com.example.appointmentscheduler;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity {

    private TextView schedDateTxt;
    private TextView schedTimeTxt;
    private EditText schedNameTxt;
    private EditText schedDescTxt;
    private EditText schedLinkTxt;
    private MaterialButton datePickerBtn;
    private MaterialButton timePickerBtn;
    private MaterialButton saveBtn;
    private SwitchMaterial editSwitch;
    private MaterialCheckBox isFinishedCheckbox;

    private String id;
    private String name;
    private String date;
    private String time;
    private String desc;
    private String link;

    private DatabaseHelper dbHelper;
    private CompoundButton.OnCheckedChangeListener finishedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        dbHelper = new DatabaseHelper(this);

        schedDateTxt = findViewById(R.id.dateText2);
        schedTimeTxt = findViewById(R.id.timeText2);
        schedNameTxt = findViewById(R.id.nameSet2);
        schedDescTxt = findViewById(R.id.descriptionSet2);
        schedLinkTxt = findViewById(R.id.linkSet2);
        datePickerBtn = findViewById(R.id.datePicker2);
        timePickerBtn = findViewById(R.id.timePicker2);
        saveBtn = findViewById(R.id.saveEditSchedButton);
        editSwitch = findViewById(R.id.edit_toggle);
        isFinishedCheckbox = findViewById(R.id.checkFinish);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar_update);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_appointment_detail);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_appointment) {
                confirmDeleteDialog();
                return true;
            }
            return false;
        });

        if (!getIntent().hasExtra("id")) {
            Toast.makeText(this, R.string.error_appointment_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        id = getIntent().getStringExtra("id");

        finishedListener = (buttonView, isChecked) ->
                dbHelper.updateAppointmentStatus(id, isChecked);

        if (!loadAppointmentFromDatabase()) {
            return;
        }

        disableEditText(schedNameTxt, schedDescTxt, schedLinkTxt);
        disableSave();
        disableTimeDatePicker();

        editSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.view.MenuItem deleteItem = toolbar.getMenu().findItem(R.id.action_delete_appointment);
            if (isChecked) {
                enableSave();
                enableTimeDatePicker();
                enableEditText(schedNameTxt, schedDescTxt, schedLinkTxt);
                if (deleteItem != null) {
                    deleteItem.setVisible(false);
                }
            } else {
                disableEditText(schedNameTxt, schedDescTxt, schedLinkTxt);
                disableSave();
                if (deleteItem != null) {
                    deleteItem.setVisible(true);
                }
                disableTimeDatePicker();
                loadAppointmentFromDatabase();
            }
        });

        saveBtn.setOnClickListener(v -> {
            name = schedNameTxt.getText().toString();
            date = schedDateTxt.getText().toString();
            time = schedTimeTxt.getText().toString();
            desc = schedDescTxt.getText().toString();
            link = schedLinkTxt.getText().toString();
            boolean isFinished = isFinishedCheckbox.isChecked();

            dbHelper.updateSchedule(id, name, date, time, desc, link);
            dbHelper.updateAppointmentStatus(id, isFinished);

            finish();
        });

        datePickerBtn.setOnClickListener(v -> showDateDialog());
        timePickerBtn.setOnClickListener(v -> showTimeDialog());
    }

    @SuppressLint("Range")
    private boolean loadAppointmentFromDatabase() {
        Cursor c = dbHelper.queryAppointmentBySchedId(id);
        if (c == null || !c.moveToFirst()) {
            Toast.makeText(this, R.string.error_appointment_missing, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        try {
            name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            if (name == null) {
                name = "";
            }
            date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
            if (date == null) {
                date = "";
            }
            time = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
            if (time == null) {
                time = "";
            }
            desc = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            if (desc == null) {
                desc = "";
            }
            link = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINK));
            if (link == null) {
                link = "";
            }
        } finally {
            c.close();
        }

        schedNameTxt.setText(name);
        schedDateTxt.setText(date);
        schedTimeTxt.setText(time);
        schedDescTxt.setText(desc);
        schedLinkTxt.setText(link);

        boolean isFinished = dbHelper.isAppointmentFinished(id);
        isFinishedCheckbox.setOnCheckedChangeListener(null);
        isFinishedCheckbox.setChecked(isFinished);
        isFinishedCheckbox.setOnCheckedChangeListener(finishedListener);
        return true;
    }

    private void enableEditText(EditText nameTxt, EditText descTxt, EditText linkTxt) {
        nameTxt.setFocusable(true);
        nameTxt.setFocusableInTouchMode(true);
        nameTxt.setClickable(true);
        nameTxt.setLongClickable(true);
        descTxt.setFocusable(true);
        descTxt.setFocusableInTouchMode(true);
        descTxt.setClickable(true);
        descTxt.setLongClickable(true);
        linkTxt.setFocusable(true);
        linkTxt.setFocusableInTouchMode(true);
        linkTxt.setClickable(true);
        linkTxt.setLongClickable(true);
    }

    private void disableEditText(EditText nameTxt, EditText descTxt, EditText linkTxt) {
        nameTxt.setFocusable(false);
        nameTxt.setFocusableInTouchMode(false);
        nameTxt.setClickable(false);
        nameTxt.setLongClickable(false);
        descTxt.setFocusable(false);
        descTxt.setFocusableInTouchMode(false);
        descTxt.setClickable(false);
        descTxt.setLongClickable(false);
        linkTxt.setFocusable(false);
        linkTxt.setFocusableInTouchMode(false);
        linkTxt.setClickable(false);
        linkTxt.setLongClickable(false);
    }

    private void disableTimeDatePicker() {
        datePickerBtn.setVisibility(View.GONE);
        timePickerBtn.setVisibility(View.GONE);
    }

    private void enableTimeDatePicker() {
        datePickerBtn.setVisibility(View.VISIBLE);
        timePickerBtn.setVisibility(View.VISIBLE);
    }

    private void disableSave() {
        saveBtn.setVisibility(View.GONE);
    }

    private void enableSave() {
        saveBtn.setVisibility(View.VISIBLE);
    }

    private void showDateDialog() {
        Calendar cal = Calendar.getInstance();
        @Nullable String current = schedDateTxt.getText() != null ? schedDateTxt.getText().toString() : null;
        if (current != null && current.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                String[] p = current.split("-");
                cal.set(Calendar.YEAR, Integer.parseInt(p[0]));
                cal.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
                // keep today
            }
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formattedMonth = String.format(Locale.US, "%02d", month + 1);
            String formattedDay = String.format(Locale.US, "%02d", dayOfMonth);
            schedDateTxt.setText(year + "-" + formattedMonth + "-" + formattedDay);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimeDialog() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            int hours = hourOfDay;
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
            String formattedMinutes = String.format(Locale.US, "%02d", minute);
            schedTimeTxt.setText(String.format(Locale.US, "%d:%s %s", hours, formattedMinutes, amPm));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
        dialog.show();
    }

    private void confirmDeleteDialog() {
        String titleName = name != null && !name.isEmpty() ? name : getString(R.string.appointment_detail_title);
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.appointment_delete_confirm_title, titleName))
                .setMessage(R.string.appointment_delete_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.appointment_delete, (dialog, which) -> {
                    dbHelper.deleteRowSchedule(id);
                    dbHelper.deleteAppointmentStatus(id);
                    finish();
                })
                .show();
    }
}
