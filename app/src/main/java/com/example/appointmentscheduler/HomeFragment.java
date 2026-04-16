package com.example.appointmentscheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final int RECENT_LIMIT = 3;
    private static final int AGENDA_LIMIT = 40;

    private DatabaseHelper dbHelper;
    private Set<String> upcomingDatesCache = Collections.emptySet();

    private MaterialButtonToggleGroup calendarModeToggle;
    private View monthPanel;
    private View weekPanel;
    private View agendaPanel;

    private TextView monthYearText;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private RecyclerView calendarRecycler;

    private TextView weekRangeText;
    private ImageButton btnPrevWeek;
    private ImageButton btnNextWeek;
    private LinearLayout weekDaysRow;

    private RecyclerView agendaRecycler;
    private HomeAgendaAdapter agendaAdapter;

    private LinearLayout recentListContainer;
    private TextView recentEmpty;

    private SimplePieChartView statsPie;
    private TextView statsSummary;
    private ProgressBar statsProgress;
    private LinearLayout statsMiniBars;

    private FloatingActionButton fabAdd;

    private final Calendar monthCalendar = Calendar.getInstance();
    private Calendar weekStartSunday;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        calendarModeToggle = view.findViewById(R.id.calendar_mode_toggle);
        monthPanel = view.findViewById(R.id.calendar_month_panel);
        weekPanel = view.findViewById(R.id.calendar_week_panel);
        agendaPanel = view.findViewById(R.id.calendar_agenda_panel);

        monthYearText = view.findViewById(R.id.text_month_year);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        calendarRecycler = view.findViewById(R.id.calendar_recycler);

        weekRangeText = view.findViewById(R.id.text_week_range);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        weekDaysRow = view.findViewById(R.id.week_days_row);

        agendaRecycler = view.findViewById(R.id.agenda_recycler);
        agendaAdapter = new HomeAgendaAdapter();
        agendaRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        agendaRecycler.setAdapter(agendaAdapter);
        agendaRecycler.setNestedScrollingEnabled(false);

        recentListContainer = view.findViewById(R.id.recent_list_container);
        recentEmpty = view.findViewById(R.id.text_recent_empty);

        statsPie = view.findViewById(R.id.stats_pie_preview);
        statsSummary = view.findViewById(R.id.stats_summary_text);
        statsProgress = view.findViewById(R.id.stats_progress_bar);
        statsMiniBars = view.findViewById(R.id.stats_mini_bars);

        fabAdd = view.findViewById(R.id.fab_add);

        weekStartSunday = startOfWeekSunday(Calendar.getInstance());

        calendarRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecycler.setNestedScrollingEnabled(false);

        btnPrevMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, -1);
            refreshMonthCalendar();
        });
        btnNextMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, 1);
            refreshMonthCalendar();
        });

        btnPrevWeek.setOnClickListener(v -> {
            weekStartSunday.add(Calendar.DAY_OF_MONTH, -7);
            refreshWeekStrip();
        });
        btnNextWeek.setOnClickListener(v -> {
            weekStartSunday.add(Calendar.DAY_OF_MONTH, 7);
            refreshWeekStrip();
        });

        calendarModeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_mode_month) {
                showCalendarMode(0);
            } else if (checkedId == R.id.btn_mode_week) {
                showCalendarMode(1);
            } else if (checkedId == R.id.btn_mode_agenda) {
                showCalendarMode(2);
            }
        });

        view.findViewById(R.id.btn_open_statistics).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), StatisticsActivity.class)));

        fabAdd.setOnClickListener(v -> addAppointmentClicked());

        showCalendarMode(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAll();
    }

    private void addAppointmentClicked() {
        AddAppointmentBottomSheet bottomSheet = new AddAppointmentBottomSheet();
        bottomSheet.setOnAppointmentAddedListener(this::reloadAll);
        bottomSheet.show(getParentFragmentManager(), "add_appointment_bottom_sheet");
    }

    private void reloadAll() {
        upcomingDatesCache = dbHelper.getUpcomingAppointmentDates();
        refreshMonthCalendar();
        refreshWeekStrip();
        refreshAgenda();
        loadRecentAppointments();
        loadStatsPreview();
    }

    private void showCalendarMode(int mode) {
        monthPanel.setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        weekPanel.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        agendaPanel.setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
        int buttonId = mode == 0 ? R.id.btn_mode_month : (mode == 1 ? R.id.btn_mode_week : R.id.btn_mode_agenda);
        if (calendarModeToggle.getCheckedButtonId() != buttonId) {
            calendarModeToggle.check(buttonId);
        }
        if (mode == 1) {
            refreshWeekStrip();
        } else if (mode == 2) {
            refreshAgenda();
        }
    }

    private void refreshMonthCalendar() {
        monthYearText.setText(monthYearTitle(monthCalendar));
        ArrayList<String> days = daysInMonthArray(monthCalendar);
        CalendarAdapter adapter = new CalendarAdapter(days, this::onCalendarDayClicked,
                monthCalendar.get(Calendar.YEAR),
                monthCalendar.get(Calendar.MONTH),
                upcomingDatesCache);
        calendarRecycler.setAdapter(adapter);
    }

    private void onCalendarDayClicked(int position, String dayText) {
        if (dayText == null || dayText.isEmpty()) {
            return;
        }
        int day = Integer.parseInt(dayText);
        String ymd = String.format(Locale.US, "%04d-%02d-%02d",
                monthCalendar.get(Calendar.YEAR),
                monthCalendar.get(Calendar.MONTH) + 1,
                day);
        showAppointmentsForDateDialog(ymd);
    }

    private void showAppointmentsForDateDialog(String ymd) {
        ArrayList<String> lines = new ArrayList<>();
        Cursor cursor = dbHelper.readUpcomingAppointmentsForDate(ymd);
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
                lines.add(time + " — " + name);
            }
        } finally {
            cursor.close();
        }
        String title = getString(R.string.appointments_on_day, ymd);
        if (lines.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(title)
                    .setMessage(R.string.no_appointments_this_day)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setItems(lines.toArray(new String[0]), null)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void refreshWeekStrip() {
        Calendar weekEnd = (Calendar) weekStartSunday.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);
        String startPart = new SimpleDateFormat("MMM d", Locale.getDefault()).format(weekStartSunday.getTime());
        String endPart = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(weekEnd.getTime());
        weekRangeText.setText(startPart + " – " + endPart);

        weekDaysRow.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        SimpleDateFormat dowFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("d", Locale.getDefault());

        Calendar day = (Calendar) weekStartSunday.clone();
        for (int i = 0; i < 7; i++) {
            String ymd = String.format(Locale.US, "%04d-%02d-%02d",
                    day.get(Calendar.YEAR),
                    day.get(Calendar.MONTH) + 1,
                    day.get(Calendar.DAY_OF_MONTH));

            View cell = inflater.inflate(R.layout.item_week_day_cell, weekDaysRow, false);
            TextView dowTv = cell.findViewById(R.id.week_cell_dow);
            TextView dateTv = cell.findViewById(R.id.week_cell_date);
            TextView countTv = cell.findViewById(R.id.week_cell_count);

            dowTv.setText(dowFormat.format(day.getTime()));
            dateTv.setText(dayNumFormat.format(day.getTime()));

            int count;
            Cursor c = dbHelper.readUpcomingAppointmentsForDate(ymd);
            try {
                count = c.getCount();
            } finally {
                c.close();
            }
            if (count == 0) {
                countTv.setText("—");
            } else {
                countTv.setText(getResources().getQuantityString(R.plurals.appointment_count_short, count, count));
            }

            final String ymdFinal = ymd;
            cell.setOnClickListener(v -> showAppointmentsForDateDialog(ymdFinal));

            weekDaysRow.addView(cell);
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void refreshAgenda() {
        ArrayList<HomeAgendaAdapter.Item> items = new ArrayList<>();
        Cursor cursor = dbHelper.readUpcomingSchedulesLimit(AGENDA_LIMIT);
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
                items.add(new HomeAgendaAdapter.Item(name, date, time));
            }
        } finally {
            cursor.close();
        }
        agendaAdapter.setItems(items);
    }

    @SuppressLint("Range")
    private void loadRecentAppointments() {
        recentListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        Cursor cursor = dbHelper.readUpcomingSchedulesLimit(RECENT_LIMIT);
        int count = 0;
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME));

                View row = inflater.inflate(R.layout.item_home_appointment_row, recentListContainer, false);
                ((TextView) row.findViewById(R.id.row_time)).setText(time);
                ((TextView) row.findViewById(R.id.row_date)).setText(date);
                ((TextView) row.findViewById(R.id.row_name)).setText(name);
                recentListContainer.addView(row);
                count++;
            }
        } finally {
            cursor.close();
        }

        if (count > 0) {
            View last = recentListContainer.getChildAt(recentListContainer.getChildCount() - 1);
            if (last != null) {
                View div = last.findViewById(R.id.row_divider);
                if (div != null) {
                    div.setVisibility(View.GONE);
                }
            }
        }

        recentEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        recentListContainer.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    private void loadStatsPreview() {
        int total = Integer.parseInt(dbHelper.getTotalAppointmentCount());
        int completed = dbHelper.getFinishedScheduleCount();

        if (total == 0) {
            statsSummary.setText(getString(R.string.stats_completed_of_total, 0, 0));
            statsPie.setCompletedFraction(0f);
            statsProgress.setProgress(0);
            statsMiniBars.removeAllViews();
            return;
        }

        statsSummary.setText(getString(R.string.stats_completed_of_total, completed, total));
        float frac = completed / (float) total;
        statsPie.setCompletedFraction(frac);
        statsProgress.setProgress(Math.round(frac * 100f));

        statsMiniBars.removeAllViews();
        ArrayList<String> months = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();
        Cursor c = dbHelper.readMonthlyCompletedCounts(6);
        try {
            while (c.moveToNext()) {
                months.add(c.getString(0));
                counts.add(c.getInt(1));
            }
        } finally {
            c.close();
        }
        Collections.reverse(months);
        Collections.reverse(counts);

        int max = 1;
        for (int n : counts) {
            max = Math.max(max, n);
        }

        float density = getResources().getDisplayMetrics().density;
        int barAreaMaxPx = (int) (48 * density);

        for (int i = 0; i < months.size(); i++) {
            String ym = months.get(i);
            int cnt = counts.get(i);
            String label = ym.length() >= 7 ? ym.substring(5) : ym;

            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            col.setLayoutParams(colLp);

            FrameLayout barFrame = new FrameLayout(requireContext());
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            barFrame.setLayoutParams(frameLp);

            View bar = new View(requireContext());
            int h = (int) (barAreaMaxPx * (cnt / (float) max));
            FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    h);
            barLp.gravity = Gravity.BOTTOM;
            bar.setLayoutParams(barLp);
            bar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ios_accent));
            barFrame.addView(bar);

            TextView tv = new TextView(requireContext());
            tv.setText(label);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.ios_label_tertiary));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);

            col.addView(barFrame);
            col.addView(tv);
            statsMiniBars.addView(col);
        }
    }

    private ArrayList<String> daysInMonthArray(Calendar cal) {
        ArrayList<String> days = new ArrayList<>();
        Calendar month = (Calendar) cal.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = month.get(Calendar.DAY_OF_WEEK);
        int emptySlots = firstDow - Calendar.SUNDAY;
        for (int i = 0; i < emptySlots; i++) {
            days.add("");
        }
        int daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= daysInMonth; d++) {
            days.add(String.valueOf(d));
        }
        return days;
    }

    private String monthYearTitle(Calendar cal) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return fmt.format(cal.getTime());
    }

    private static Calendar startOfWeekSunday(Calendar any) {
        Calendar c = (Calendar) any.clone();
        int dow = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DAY_OF_MONTH, -(dow - Calendar.SUNDAY));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }
}
