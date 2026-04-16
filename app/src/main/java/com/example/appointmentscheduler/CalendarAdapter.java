package com.example.appointmentscheduler;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final String currentDate;
    private final int currentYear;
    private final int currentMonth;
    private final int displayedYear;
    private final int displayedMonth;
    private final Set<String> appointmentDates;


    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, int displayedYear, int displayedMonth,
                           Set<String> appointmentDates) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.displayedYear = displayedYear;
        this.displayedMonth = displayedMonth;
        this.appointmentDates = appointmentDates != null ? appointmentDates : java.util.Collections.emptySet();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());
        currentDate = dateFormat.format(calendar.getTime());
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        float density = parent.getResources().getDisplayMetrics().density;
        layoutParams.height = (int) (42 * density);
        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        String day = daysOfMonth.get(position);
        holder.dayOfMonth.setText(day);

        boolean isEmptyCell = day.isEmpty();
        holder.itemView.setAlpha(isEmptyCell ? 0f : 1f);
        holder.itemView.setClickable(!isEmptyCell);

        if (isEmptyCell) {
            holder.squareView.setVisibility(View.GONE);
            holder.dayOfMonth.setTextColor(Color.WHITE);
            holder.appointmentDot.setVisibility(View.GONE);
            return;
        }

        if (day.equals(currentDate) && displayedYear == currentYear && displayedMonth == currentMonth) {
            holder.squareView.setVisibility(View.VISIBLE);
            holder.dayOfMonth.setTextColor(Color.WHITE);
        } else {
            holder.squareView.setVisibility(View.GONE);
            holder.dayOfMonth.setTextColor(Color.WHITE);
        }

        String ymd = String.format(Locale.US, "%04d-%02d-%02d", displayedYear, displayedMonth + 1, Integer.parseInt(day));
        boolean hasAppt = appointmentDates.contains(ymd);
        holder.appointmentDot.setVisibility(hasAppt ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dayOfMonth;
        public final View squareView;
        public final View appointmentDot;
        private final OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            squareView = itemView.findViewById(R.id.squareView);
            appointmentDot = itemView.findViewById(R.id.appointmentDot);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String day = dayOfMonth.getText().toString();
            if (day.isEmpty()) {
                return;
            }
            onItemListener.onItemClick(getAdapterPosition(), day);
        }
    }

}