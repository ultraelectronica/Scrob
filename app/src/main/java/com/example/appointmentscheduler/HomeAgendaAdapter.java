package com.example.appointmentscheduler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class HomeAgendaAdapter extends RecyclerView.Adapter<HomeAgendaAdapter.Holder> {

    static class Item {
        final String name;
        final String date;
        final String time;

        Item(String name, String date, String time) {
            this.name = name;
            this.date = date;
            this.time = time;
        }
    }

    private final List<Item> items = new ArrayList<>();

    void setItems(List<Item> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_appointment_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Item it = items.get(position);
        holder.time.setText(it.time);
        holder.date.setText(it.date);
        holder.name.setText(it.name);
        holder.divider.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView time;
        final TextView date;
        final TextView name;
        final View divider;

        Holder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.row_time);
            date = itemView.findViewById(R.id.row_date);
            name = itemView.findViewById(R.id.row_name);
            divider = itemView.findViewById(R.id.row_divider);
        }
    }
}
