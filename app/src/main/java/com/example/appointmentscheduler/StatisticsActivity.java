package com.example.appointmentscheduler;

import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.core.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.statistics_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        DatabaseHelper db = new DatabaseHelper(this);

        int total = Integer.parseInt(db.getTotalAppointmentCount());
        int completed = db.getFinishedScheduleCount();
        int pending = db.getPendingScheduleCount();

        TextView summary = findViewById(R.id.stats_full_summary);
        TextView pendingTv = findViewById(R.id.stats_full_pending);
        SimplePieChartView pie = findViewById(R.id.stats_pie_full);
        ProgressBar progress = findViewById(R.id.stats_progress_full);
        LinearLayout bars = findViewById(R.id.stats_full_bars);
        TextView detail = findViewById(R.id.stats_full_detail);

        pendingTv.setText(getString(R.string.stats_pending, pending));

        if (total == 0) {
            summary.setText(getString(R.string.stats_completed_of_total, 0, 0));
            pie.setCompletedFraction(0f);
            progress.setProgress(0);
            bars.removeAllViews();
            detail.setText(getString(R.string.stats_detail_template, 0, 0, 0));
            return;
        }

        summary.setText(getString(R.string.stats_completed_of_total, completed, total));
        float frac = completed / (float) total;
        pie.setCompletedFraction(frac);
        progress.setProgress(Math.round(frac * 100f));

        detail.setText(getString(R.string.stats_detail_template, total, completed, pending));

        ArrayList<String> months = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();
        Cursor c = db.readMonthlyCompletedCounts(12);
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
        int barAreaMaxPx = (int) (160 * density);

        bars.removeAllViews();
        for (int i = 0; i < months.size(); i++) {
            String ym = months.get(i);
            int cnt = counts.get(i);
            String label = ym.length() >= 7 ? ym.substring(5) : ym;

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            col.setLayoutParams(colLp);

            FrameLayout barFrame = new FrameLayout(this);
            LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            barFrame.setLayoutParams(frameLp);

            View bar = new View(this);
            int h = (int) (barAreaMaxPx * (cnt / (float) max));
            FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    h);
            barLp.gravity = Gravity.BOTTOM;
            bar.setLayoutParams(barLp);
            bar.setBackgroundColor(ContextCompat.getColor(this, R.color.ios_accent));
            barFrame.addView(bar);

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tv.setTextColor(ContextCompat.getColor(this, R.color.ios_label_tertiary));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);

            col.addView(barFrame);
            col.addView(tv);
            bars.addView(col);
        }
    }
}
