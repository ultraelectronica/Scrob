package com.example.appointmentscheduler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class HomeAgendaAdapter extends RecyclerView.Adapter<HomeAgendaAdapter.Holder> {

    interface OnAgendaItemClickListener {
        void onAgendaItemClick(long schedId);
    }

    static class Item {
        final long schedId;
        final String name;
        final String date;
        final String time;

        Item(long schedId, String name, String date, String time) {
            this.schedId = schedId;
            this.name = name;
            this.date = date;
            this.time = time;
        }
    }

    private final List<Item> items = new ArrayList<>();
    @Nullable
    private OnAgendaItemClickListener itemClickListener;

    void setOnAgendaItemClickListener(@Nullable OnAgendaItemClickListener listener) {
        this.itemClickListener = listener;
    }

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
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onAgendaItemClick(it.schedId);
            }
        });
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
