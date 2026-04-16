package com.example.appointmentscheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final Context context;
    private List<Appointment> appointments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ScheduleAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    public ScheduleAdapter(Context context, ArrayList<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    public ScheduleAdapter(Context context, Object activity, ArrayList<String> array_id, ArrayList<String> array_name, ArrayList<String> array_date, ArrayList<String> array_time, ArrayList<String> array_description, ArrayList<String> array_link, ArrayList<Boolean> array_isFinished) {
        this.context = context;
        this.appointments = new ArrayList<>();
        for (int i = 0; i < array_id.size(); i++) {
            this.appointments.add(new Appointment(
                Integer.parseInt(array_id.get(i)),
                array_name.get(i),
                array_date.get(i),
                array_time.get(i),
                array_link.get(i),
                array_description.get(i)
            ));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.timeText.setText(appointment.getTime());
        holder.dateText.setText(appointment.getDate());
        holder.titleText.setText(appointment.getTitle());
        
        String location = appointment.getLocation();
        if (location != null && !location.isEmpty()) {
            holder.locationText.setText(location);
            holder.locationText.setVisibility(View.VISIBLE);
        } else {
            holder.locationText.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, dateText, titleText, locationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.row_time);
            dateText = itemView.findViewById(R.id.row_date);
            titleText = itemView.findViewById(R.id.row_name);
            locationText = itemView.findViewById(R.id.row_location);
        }
    }
}
