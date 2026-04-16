package com.example.appointmentscheduler;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;

public class AddAppointmentBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_App_BottomSheetDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        return dialog;
    }

    public interface OnAppointmentAddedListener {
        void onAppointmentAdded();
    }

    private DatabaseHelper dbHelper;
    private OnAppointmentAddedListener listener;

    private EditText nameInput;
    private EditText descInput;
    private EditText linkInput;
    private TextView dateText;
    private TextView timeText;

    public void setOnAppointmentAddedListener(OnAppointmentAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        nameInput = view.findViewById(R.id.nameSet);
        descInput = view.findViewById(R.id.descriptionSet);
        linkInput = view.findViewById(R.id.linkSet);
        dateText = view.findViewById(R.id.dateText);
        timeText = view.findViewById(R.id.timeText);
        MaterialButton datePicker = view.findViewById(R.id.datePicker);
        MaterialButton timePicker = view.findViewById(R.id.timePicker);
        MaterialButton submitButton = view.findViewById(R.id.submitSchedButton);

        datePicker.setOnClickListener(v -> openDateDialog());
        timePicker.setOnClickListener(v -> openTimeDialog());
        submitButton.setOnClickListener(v -> saveAppointment());
    }

    private void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String formattedMonth = String.format("%02d", month + 1);
            String formattedDay = String.format("%02d", dayOfMonth);
            dateText.setText(year + "-" + formattedMonth + "-" + formattedDay);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void openTimeDialog() {
        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, hours, minutes) -> {
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
        }, 12, 0, false);
        dialog.show();
    }

    private void saveAppointment() {
        String name = nameInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String link = linkInput.getText().toString().trim();
        String date = dateText.getText().toString().trim();
        String time = timeText.getText().toString().trim();

        ArrayList<String> missing = new ArrayList<>();
        if (TextUtils.isEmpty(name)) missing.add("Name");
        if (TextUtils.isEmpty(date)) missing.add("Date");
        if (TextUtils.isEmpty(description)) missing.add("Description");
        if (TextUtils.isEmpty(time)) missing.add("Time");

        if (!missing.isEmpty()) {
            Toast.makeText(requireContext(), "The following fields cannot be empty: " + TextUtils.join(", ", missing), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.addAppointment(name, date, description, time, link);
        if (success) {
            Toast.makeText(requireContext(), "Appointment saved", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onAppointmentAdded();
            }
            dismiss();
        } else {
            Toast.makeText(requireContext(), "Error saving appointment", Toast.LENGTH_SHORT).show();
        }
    }
}
